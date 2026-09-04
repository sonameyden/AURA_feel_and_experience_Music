package com.aura.feature.moodinput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.audio.AuraPlayer
import com.aura.core.common.util.AppError
import com.aura.core.common.util.AppResult
import com.aura.core.data.repository.AtmosphereRepository
import com.aura.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * "How are you feeling?" flow — per Section 9 of the project spec, this calls
 * YOUR backend (never OpenAI directly), which interprets the mood text,
 * queries the real catalog, and returns a matched song + AtmosphereProfile.
 */
@HiltViewModel
class MoodInputViewModel @Inject constructor(
    private val atmosphereRepository: AtmosphereRepository,
    private val player: AuraPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow<MoodInputUiState>(MoodInputUiState.Idle)
    val uiState: StateFlow<MoodInputUiState> = _uiState.asStateFlow()

    fun onSubmit(moodText: String) {
        if (moodText.isBlank()) return
        viewModelScope.launch {
            _uiState.value = MoodInputUiState.Loading
            when (val result = atmosphereRepository.getAtmosphereForMood(moodText)) {
                is AppResult.Success -> {
                    val response = result.data
                    val song = response.song
                    if (response.mode == "direct_play" && song != null) {
                        // For direct play, we start the song immediately
                        player.play(song)
                        _uiState.value = MoodInputUiState.DirectPlay(song)
                    } else {
                        _uiState.value = MoodInputUiState.Recommendations(response.reply, response.songs)
                    }
                }
                is AppResult.Error -> {
                    val error = result.error
                    val msg = when (error) {
                        is AppError.Unknown -> error.message
                        AppError.Network -> "Network connection issue."
                        AppError.Unauthorized -> "Backend authorization failed."
                        AppError.Server -> "Backend server error."
                        else -> null
                    } ?: "Couldn't find a match — try again."
                    _uiState.value = MoodInputUiState.Error(msg)
                }
                AppResult.Loading -> Unit
            }
        }
    }

    fun onSongClick(song: Song) {
        player.play(song)
    }
}
