package com.aura.feature.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.audio.AuraPlayer
import com.aura.core.common.util.AppResult
import com.aura.core.data.repository.PlaylistRepository
import com.aura.core.data.repository.SongRepository
import com.aura.core.model.Playlist
import com.aura.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    private val player: AuraPlayer
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"])

    private val _uiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = PlaylistUiState.Loading
            
            val playlistResult = playlistRepository.getPlaylist(playlistId)
            
            if (playlistResult is AppResult.Success) {
                val playlist = playlistResult.data
                val songs = playlist.songIds.mapNotNull { id ->
                    val result = songRepository.getSong(id)
                    (result as? AppResult.Success)?.data
                }
                _uiState.value = PlaylistUiState.Success(playlist, songs)
            } else {
                _uiState.value = PlaylistUiState.Error("Failed to load playlist detail.")
            }
        }
    }

    fun onSongClick(song: Song, queue: List<Song>) {
        val startIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        player.playQueue(queue, startIndex)
    }
}

sealed interface PlaylistUiState {
    data object Loading : PlaylistUiState
    data class Success(val playlist: Playlist, val songs: List<Song>) : PlaylistUiState
    data class Error(val message: String) : PlaylistUiState
}
