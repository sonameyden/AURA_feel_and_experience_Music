package com.aura.feature.moodinput

import com.aura.core.model.Song

sealed interface MoodInputUiState {
    data object Idle : MoodInputUiState
    data object Loading : MoodInputUiState
    
    /** AI found a specific song for a direct command (e.g. "Play Starboy"). */
    data class DirectPlay(val song: Song) : MoodInputUiState
    
    /** AI understood a general mood and suggests several options. */
    data class Recommendations(val reply: String, val songs: List<Song>) : MoodInputUiState
    
    data class Error(val message: String) : MoodInputUiState
}
