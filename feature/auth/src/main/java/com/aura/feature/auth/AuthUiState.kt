package com.aura.feature.auth

/** Shared by both Login and SignUp screens — same shape, same states. */
sealed interface AuthFormUiState {
    data object Idle : AuthFormUiState
    data object Loading : AuthFormUiState
    data object Success : AuthFormUiState
    data class Error(val message: String) : AuthFormUiState
}
