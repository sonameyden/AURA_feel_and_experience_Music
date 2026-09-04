package com.aura.feature.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.common.util.AppResult
import com.aura.core.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    fun uploadSong(title: String, genre: String, file: File) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            when (val result = songRepository.uploadSong(title, genre, file)) {
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
