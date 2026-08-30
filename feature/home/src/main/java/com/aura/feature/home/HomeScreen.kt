package com.aura.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aura.core.designsystem.components.GlassCard

/**
 * Deliberately plain/neutral — per Section 2 & 5 of the project spec, Home
 * has NO environment animation. Immersion here comes from typography,
 * spacing, and soft glass-card depth, not motion. Only mood tiles carry a
 * static accent color hinting at their environment.
 */
@Composable
fun HomeScreen(
    onSongClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onMoodClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { padding: PaddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(text = "AURA", style = MaterialTheme.typography.displayLarge)
            Text(text = "Step into it.", style = MaterialTheme.typography.bodyMedium)

            GlassCard(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Search music, artists, albums...", modifier = Modifier
                        .clickable { onSearchClick() }
                        .padding(bottom = 8.dp))
                }
            }

            when (val state = uiState) {
                is HomeUiState.Ready -> {
                    Text(text = "Trending", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 24.dp))
                    LazyRow {
                        items(state.trending) { song ->
                            GlassCard(modifier = Modifier.padding(8.dp)) {
                                Text(text = song.title, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }
                is HomeUiState.Loading -> Text(text = "Loading...")
                is HomeUiState.Error -> Text(text = state.message)
            }
        }
    }
}