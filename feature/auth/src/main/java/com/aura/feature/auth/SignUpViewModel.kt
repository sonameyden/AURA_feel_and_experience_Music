package com.aura.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.auth.AuthRepository
import com.aura.core.common.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthFormUiState>(AuthFormUiState.Idle)
    val uiState: StateFlow<AuthFormUiState> = _uiState.asStateFlow()

    fun onSignUpClick(email: String, password: String, confirmPassword: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthFormUiState.Error("Enter an email and password.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthFormUiState.Error("Password must be at least 6 characters.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthFormUiState.Error("Passwords don't match.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthFormUiState.Loading
            when (val result = authRepository.signUp(email, password)) {
                is AppResult.Success -> _uiState.value = AuthFormUiState.Success
                is AppResult.Error -> _uiState.value = AuthFormUiState.Error("Couldn't create your account — try a different email.")
                AppResult.Loading -> Unit
            }
        }
    }
}
