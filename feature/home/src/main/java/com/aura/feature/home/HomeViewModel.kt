package com.aura.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.audio.AuraPlayer
import com.aura.core.audio.PlaybackState
import com.aura.core.common.util.AppResult
import com.aura.core.data.repository.ArtistRepository
import com.aura.core.data.repository.SongRepository
import com.aura.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val artistRepository: ArtistRepository,
    private val player: AuraPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Song id currently loaded in AuraPlayer, or null — used to highlight the playing card. */
    val currentPlayingSongId: StateFlow<String?> = player.playbackState.map { state ->
        when (state) {
            is PlaybackState.Playing -> state.song.id
            is PlaybackState.Paused -> state.song.id
            else -> null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val trendingDeferred = async { songRepository.getTrending() }
            val recommendationsDeferred = async { songRepository.getRecommendations() }
            val artistsDeferred = async { artistRepository.getArtists() }

            val trendingResult = trendingDeferred.await()
            val recommendationsResult = recommendationsDeferred.await()
            val artistsResult = artistsDeferred.await()

            val trending = (trendingResult as? AppResult.Success)?.data ?: emptyList()
            val recommendations = (recommendationsResult as? AppResult.Success)?.data ?: emptyList()
            val artists = (artistsResult as? AppResult.Success)?.data ?: emptyList()

            Log.d("HomeViewModel", "Loaded ${trending.size} trending, ${artists.size} artists, ${recommendations.size} recommendations")

            _uiState.value = HomeUiState.Ready(
                recentlyPlayed = emptyList(),
                trending = trending,
                recommendedArtists = artists,
                recommendations = recommendations
            )
        }
    }

    fun onSongClick(song: Song) {
        val state = _uiState.value
        val queue = (state as? HomeUiState.Ready)?.trending?.takeIf { it.isNotEmpty() } ?: listOf(song)
        val startIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        player.playQueue(queue, startIndex)
    }
}
