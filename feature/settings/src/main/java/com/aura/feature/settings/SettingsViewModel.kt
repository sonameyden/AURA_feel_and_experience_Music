package com.aura.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.audio.AuraPlayer
import com.aura.core.auth.AuthRepository
import com.aura.core.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val auraPlayer: AuraPlayer
) : ViewModel() {

    val currentUserId: StateFlow<String?> = authRepository.authState.map { state ->
        (state as? AuthState.Authenticated)?.userId
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun logout() {
        auraPlayer.stop()
        authRepository.signOut()
    }
}
