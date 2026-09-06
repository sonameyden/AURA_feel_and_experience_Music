package com.aura.feature.artist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.common.util.AppError
import com.aura.core.common.util.AppResult
import com.aura.core.data.repository.ArtistRepository
import com.aura.core.data.repository.SongRepository
import com.aura.core.model.Artist
import com.aura.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val artistRepository: ArtistRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ArtistProfileState>(ArtistProfileState.Loading)
    val profileState: StateFlow<ArtistProfileState> = _profileState.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private var currentArtistId: String? = null

    fun loadArtistProfile(artistId: String, silent: Boolean = false) {
        this.currentArtistId = artistId
        viewModelScope.launch {
            if (!silent) {
                _profileState.value = ArtistProfileState.Loading
            }
            
            val idToFetch = if (artistId == "me" || artistId == "current_user") "me" else artistId

            val artistDeferred = async { artistRepository.getArtist(idToFetch) }
            val songsDeferred = async { artistRepository.getSongsByArtist(idToFetch) }
            
            val artistResult = artistDeferred.await()
            val songsResult = songsDeferred.await()
            
            if (artistResult is AppResult.Success) {
                _profileState.value = ArtistProfileState.Success(
                    artist = artistResult.data,
                    songs = (songsResult as? AppResult.Success)?.data ?: emptyList()
                )
            } else if (!silent) {
                val errorMsg = (artistResult as? AppResult.Error)?.error?.let { "Artist Error: $it" } 
                    ?: "Could not load artist profile."
                _profileState.value = ArtistProfileState.Error(errorMsg)
            }
        }
    }

    fun becomeArtist(name: String, bio: String) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            val result = artistRepository.createArtistProfile(name, bio)
            if (result is AppResult.Success) {
                _uploadState.value = UploadState.Success
            } else {
                _uploadState.value = UploadState.Error("Failed to create profile.")
            }
        }
    }

    fun uploadProfileImage(file: File) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            when (val result = artistRepository.uploadProfileImage(file)) {
                is AppResult.Success -> {
                    _uploadState.value = UploadState.Success
                    currentArtistId?.let { loadArtistProfile(it, silent = true) }
                }
                is AppResult.Error -> _uploadState.value = UploadState.Error("Image upload failed.")
                AppResult.Loading -> Unit
            }
        }
    }

    fun uploadSong(
        title: String,
        artistName: String,
        genre: String,
        durationMs: Long,
        audioFile: File,
        artworkFile: File?
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            when (val result = songRepository.uploadSong(title, artistName, genre, durationMs, audioFile, artworkFile)) {
                is AppResult.Success -> {
                    _uploadState.value = UploadState.Success
                    currentArtistId?.let { loadArtistProfile(it, silent = true) }
                }
                is AppResult.Error -> _uploadState.value = UploadState.Error("Upload failed. Try again.")
                AppResult.Loading -> Unit
            }
        }
    }

    fun updateSongArtwork(songId: String, artworkFile: File) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            when (val result = songRepository.updateSongArtwork(songId, artworkFile)) {
                is AppResult.Success -> {
                    _uploadState.value = UploadState.Success
                    currentArtistId?.let { loadArtistProfile(it, silent = true) }
                }
                is AppResult.Error -> _uploadState.value = UploadState.Error("Artwork update failed.")
                AppResult.Loading -> Unit
            }
        }
    }

    fun deleteSong(songId: String) {
        viewModelScope.launch {
            Log.d("ArtistViewModel", "Deleting song: $songId")
            _uploadState.value = UploadState.Loading
            
            // Optimistic update: remove from list immediately
            val previousState = _profileState.value
            if (previousState is ArtistProfileState.Success) {
                _profileState.value = previousState.copy(
                    songs = previousState.songs.filter { it.id != songId }
                )
            }

            when (val result = songRepository.deleteSong(songId)) {
                is AppResult.Success -> {
                    Log.d("ArtistViewModel", "Delete success")
                    _uploadState.value = UploadState.Success
                    // No need to reload full profile, we already removed it from the list
                }
                is AppResult.Error -> {
                    val msg = (result.error as? AppError.Unknown)?.message ?: "Delete failed"
                    Log.e("ArtistViewModel", "Delete failed: $msg")
                    _uploadState.value = UploadState.Error(msg)
                    // Rollback on failure
                    if (previousState is ArtistProfileState.Success) {
                        _profileState.value = previousState
                    }
                }
                AppResult.Loading -> Unit
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}

sealed interface UploadState {
    data object Idle : UploadState
    data object Loading : UploadState
    data object Success : UploadState
    data class Error(val message: String) : UploadState
}

sealed interface ArtistProfileState {
    data object Loading : ArtistProfileState
    data class Success(val artist: Artist, val songs: List<Song>) : ArtistProfileState
    data class Error(val message: String) : ArtistProfileState
}
