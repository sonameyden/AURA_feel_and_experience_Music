package com.aura.feature.moodinput

sealed interface MoodInputUiState {
    data object Idle : MoodInputUiState
    data object Loading : MoodInputUiState
    data class Matched(val songId: String) : MoodInputUiState
    data class Error(val message: String) : MoodInputUiState
}
