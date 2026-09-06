package com.aura.feature.home

import com.aura.core.model.Artist
import com.aura.core.model.Song

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(
        val recentlyPlayed: List<Song>,
        val trending: List<Song>,
        val recommendedArtists: List<Artist>,
        val recommendations: List<Song>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
