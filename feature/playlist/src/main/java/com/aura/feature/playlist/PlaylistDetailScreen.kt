package com.aura.feature.playlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(text = "Playlist: $playlistId")
            // TODO Phase 4: song list, reorder, share, aggregate-mood cover tint (Playlist.coverColorHex)
        }
    }
}
