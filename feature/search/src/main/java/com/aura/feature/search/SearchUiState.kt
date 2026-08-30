package com.aura.feature.search

import com.aura.core.model.Song

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Results(val songs: List<Song>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
