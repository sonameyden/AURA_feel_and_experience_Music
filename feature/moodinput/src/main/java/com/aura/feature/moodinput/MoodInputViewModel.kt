package com.aura.feature.moodinput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.common.util.AppResult
import com.aura.core.data.repository.AtmosphereRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * "How are you feeling?" flow — per Section 9 of the project spec, this calls
 * YOUR backend (never OpenAI directly), which interprets the mood text,
 * queries the real catalog, and returns a matched song + AtmosphereProfile.
 */
@HiltViewModel
class MoodInputViewModel @Inject constructor(
    private val atmosphereRepository: AtmosphereRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MoodInputUiState>(MoodInputUiState.Idle)
    val uiState: StateFlow<MoodInputUiState> = _uiState.asStateFlow()

    fun onSubmit(moodText: String) {
        if (moodText.isBlank()) return
        viewModelScope.launch {
            _uiState.value = MoodInputUiState.Loading
            when (val result = atmosphereRepository.getAtmosphereForMood(moodText)) {
                is AppResult.Success -> _uiState.value = MoodInputUiState.Matched(result.data.songId)
                is AppResult.Error -> _uiState.value = MoodInputUiState.Error("Couldn't find a match — try again.")
                AppResult.Loading -> Unit
            }
        }
    }
}
