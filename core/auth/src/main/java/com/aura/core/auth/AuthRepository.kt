package com.aura.core.auth

import com.aura.core.common.util.AppDispatchers
import com.aura.core.common.util.AppError
import com.aura.core.common.util.AppResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for authentication. Every screen in the app —
 * AuraNavHost's auth gate, SettingsScreen's logout button, NowPlayingViewModel
 * if it ever needs the current user id for listening history — reads this,
 * never FirebaseAuth directly.
 *
 * Firebase's SDK manages ID token storage/refresh internally and securely;
 * we never manually persist a raw token in SharedPreferences (per the
 * "no plain SharedPreferences token storage" best practice).
 */
@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dispatchers: AppDispatchers
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.default)

    /** Live auth state, observed by AuraNavHost to gate Home vs Login. */
    val authState: StateFlow<AuthState> = observeAuthState()
        .stateIn(scope, SharingStarted.Eagerly, AuthState.Loading)

    private fun observeAuthState(): Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            trySend(
                if (user == null) AuthState.Unauthenticated
                else AuthState.Authenticated(userId = user.uid, email = user.email)
            )
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(email: String, password: String): AppResult<Unit> = withContext(dispatchers.io) {
        runCatching {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Error(it.toAuthError()) }
        )
    }

    suspend fun signIn(email: String, password: String): AppResult<Unit> = withContext(dispatchers.io) {
        runCatching {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Error(it.toAuthError()) }
        )
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    /**
     * The Firebase ID token — attach this as `Authorization: Bearer <token>` on
     * every backend call once Phase 2's endpoints are protected. Firebase
     * refreshes this automatically; always fetch fresh rather than caching it
     * yourself.
     */
    suspend fun getIdToken(): String? = withContext(dispatchers.io) {
        runCatching { 
            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
            if (token != null) {
                println("DEBUG_TOKEN: $token")
            }
            token
        }.getOrNull()
    }
}

private fun Throwable.toAuthError(): AppError = when (this) {
    is FirebaseAuthException -> AppError.Unauthorized
    else -> AppError.Unknown(message)
}
