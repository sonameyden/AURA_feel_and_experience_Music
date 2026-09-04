package com.aura.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.audio.AuraPlayer
import com.aura.core.audio.PlaybackState
import com.aura.core.common.util.AppResult
import com.aura.core.data.repository.SongRepository
import com.aura.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
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
            val trendingResult = songRepository.getTrending()
            val recommendationsResult = songRepository.getRecommendations()

            val trending = (trendingResult as? AppResult.Success)?.data ?: emptyList()
            val recommendations = (recommendationsResult as? AppResult.Success)?.data ?: emptyList()

            _uiState.value = HomeUiState.Ready(
                recentlyPlayed = emptyList(), // TODO: wire to local listening-history table
                trending = trending,
                recommendations = recommendations
            )
        }
    }

    /**
     * Starts playback immediately, on the first tap, using the currently
     * displayed Trending row as the playback queue — so Next/Previous on
     * Now Playing walk through the same list the user was browsing.
     * Navigation to Now Playing is handled separately by the caller; this
     * function alone is what makes tapping a song play it right away.
     */
    fun onSongClick(song: Song) {
        val state = _uiState.value
        val queue = (state as? HomeUiState.Ready)?.trending?.takeIf { it.isNotEmpty() } ?: listOf(song)
        val startIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        player.playQueue(queue, startIndex)
    }
}
