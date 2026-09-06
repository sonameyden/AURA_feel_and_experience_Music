package com.aura.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.audio.AuraPlayer
import com.aura.core.auth.AuthRepository
import com.aura.core.auth.AuthState
import com.aura.core.common.util.AppError
import com.aura.core.common.util.AppResult
import com.aura.core.data.repository.LibraryRepository
import com.aura.core.data.repository.PlaylistRepository
import com.aura.core.model.Playlist
import com.aura.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val libraryRepository: LibraryRepository,
    private val authRepository: AuthRepository,
    private val player: AuraPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authState.collect { auth ->
                if (auth is AuthState.Authenticated) {
                    load()
                } else if (auth is AuthState.Unauthenticated) {
                    _uiState.value = LibraryUiState.Error("Please log in to see your library.")
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            val auth = authRepository.authState.value
            if (auth !is AuthState.Authenticated) return@launch

            if (_uiState.value !is LibraryUiState.Success) {
                _uiState.value = LibraryUiState.Loading
            }
            
            val likedDeferred = async { libraryRepository.getLikedSongs() }
            val historyDeferred = async { libraryRepository.getHistory() }
            val playlistsDeferred = async { playlistRepository.getUserPlaylists() }
            
            val likedResult = likedDeferred.await()
            val historyResult = historyDeferred.await()
            val playlistsResult = playlistsDeferred.await()
            
            val liked = (likedResult as? AppResult.Success)?.data ?: emptyList()
            val history = (historyResult as? AppResult.Success)?.data ?: emptyList()
            val playlists = (playlistsResult as? AppResult.Success)?.data ?: emptyList()

            if (likedResult is AppResult.Error && historyResult is AppResult.Error && playlistsResult is AppResult.Error) {
                _uiState.value = LibraryUiState.Error("Sync failed. Check connection.")
            } else {
                _uiState.value = LibraryUiState.Success(
                    likedSongs = liked,
                    history = history,
                    playlists = playlists,
                    isRefreshing = false
                )
                
                // Detailed logging for development
                if (playlistsResult is AppResult.Error) {
                    println("Playlist Sync Detail: ${playlistsResult.error}")
                }
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            _uiState.update { if (it is LibraryUiState.Success) it.copy(isRefreshing = true) else it }
            val result = playlistRepository.createPlaylist(name)
            if (result is AppResult.Success) {
                load() 
            } else {
                _uiState.update { if (it is LibraryUiState.Success) it.copy(isRefreshing = false) else it }
            }
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            val result = playlistRepository.addSongToPlaylist(playlistId, songId)
            if (result is AppResult.Success) {
                load() 
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            val result = playlistRepository.deletePlaylist(playlistId)
            if (result is AppResult.Success) {
                load()
            }
        }
    }

    fun onSongClick(song: Song, queue: List<Song>) {
        val startIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        player.playQueue(queue, startIndex)
    }

    private fun AppError.toMessage(): String = when (this) {
        AppError.Network -> "Network issue."
        AppError.Unauthorized -> "Auth failed."
        is AppError.Unknown -> message ?: "Data error."
        else -> "Error."
    }
}

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data class Success(
        val likedSongs: List<Song>,
        val history: List<Song>,
        val playlists: List<Playlist> = emptyList(),
        val isRefreshing: Boolean = false
    ) : LibraryUiState
    data class Error(val message: String) : LibraryUiState
}
