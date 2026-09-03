package com.aura.core.auth

/**
 * Explicit sealed auth state — modeled per the best-practices doc (no
 * independent booleans like isLoggedIn/isLoading floating around separately).
 * AuraNavHost observes this to decide whether to show Login or Home.
 */
sealed interface AuthState {
    data object Loading : AuthState                 // initial value, before Firebase reports the current user
    data object Unauthenticated : AuthState
    data class Authenticated(val userId: String, val email: String?) : AuthState
}
