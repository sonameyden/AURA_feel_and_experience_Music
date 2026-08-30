package com.aura.feature.artist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ArtistProfileScreen(
    artistId: String,
    onUploadClick: () -> Unit,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(text = "Artist: $artistId")
            // TODO Phase 4: bio, followers, popular songs, albums, related artists
            Button(onClick = onUploadClick, modifier = Modifier.padding(top = 16.dp)) {
                Text("Upload music")
            }
        }
    }
}
