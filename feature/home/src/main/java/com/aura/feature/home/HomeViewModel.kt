package com.aura.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.common.util.AppResult
import com.aura.core.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
}
