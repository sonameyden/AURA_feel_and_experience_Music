package com.aura.feature.settings

import androidx.lifecycle.ViewModel
import com.aura.core.audio.AuraPlayer
import com.aura.core.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val auraPlayer: AuraPlayer
) : ViewModel() {
    // TODO Phase 5: persist reduced-motion / visual-intensity-tier / cat-visibility via DataStore, expose as StateFlow

    fun logout() {
        auraPlayer.stop()
        authRepository.signOut()
    }
}
