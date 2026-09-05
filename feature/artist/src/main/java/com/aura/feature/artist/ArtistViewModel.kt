package com.aura.feature.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun loadArtistProfile(artistId: String) {
        viewModelScope.launch {
            _profileState.value = ArtistProfileState.Loading
            
            val artistDeferred = async { artistRepository.getArtist(artistId) }
            val songsDeferred = async { artistRepository.getSongsByArtist(artistId) }
            
            val artistResult = artistDeferred.await()
            val songsResult = songsDeferred.await()
            
            if (artistResult is AppResult.Success) {
                _profileState.value = ArtistProfileState.Success(
                    artist = artistResult.data,
                    songs = (songsResult as? AppResult.Success)?.data ?: emptyList()
                )
            } else {
                val errorMsg = (artistResult as? AppResult.Error)?.error?.let { "Artist Error: $it" } 
                    ?: "Could not load artist details."
                _profileState.value = ArtistProfileState.Error(errorMsg)
            }
        }
    }

    fun uploadSong(
        title: String,
        artistName: String,
        genre: String,
        durationMs: Long,
        file: File
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            when (val result = songRepository.uploadSong(title, artistName, genre, durationMs, file)) {
                is AppResult.Success -> _uploadState.value = UploadState.Success
                is AppResult.Error -> _uploadState.value = UploadState.Error("Upload failed. Try again.")
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
