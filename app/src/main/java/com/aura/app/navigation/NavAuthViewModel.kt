package com.aura.app.navigation

import androidx.lifecycle.ViewModel
import com.aura.core.auth.AuthRepository
import com.aura.core.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Thin ViewModel that just exposes AuthRepository.authState to the NavHost's
 * AuthGate composable. Kept separate from any feature ViewModel since it's
 * nav-graph plumbing, not feature logic.
 */
@HiltViewModel
class NavAuthViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {
    val authState: StateFlow<AuthState> = authRepository.authState
}
