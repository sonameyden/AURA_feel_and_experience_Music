package com.aura.feature.auth

import android.util.Log
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthFormUiState>(AuthFormUiState.Idle)
    val uiState: StateFlow<AuthFormUiState> = _uiState.asStateFlow()

    fun onLoginClick(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthFormUiState.Error("Enter your email and password.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthFormUiState.Loading
            when (val result = authRepository.signIn(email, password)) {
                is AppResult.Success -> {
                    val token = authRepository.getIdToken()
                    Log.d("AURA_TOKEN", "Login Success. Bearer $token")
                    _uiState.value = AuthFormUiState.Success
                }
                is AppResult.Error -> _uiState.value = AuthFormUiState.Error("Couldn't sign in — check your email and password.")
                AppResult.Loading -> Unit
            }
        }
    }
}
