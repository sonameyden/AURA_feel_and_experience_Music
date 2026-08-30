package com.aura.feature.artist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Phase 2 (per Section 10 of the project spec) — artist selects a file, app
 * uploads it to the backend, which stores it in R2 and writes catalog
 * metadata to Supabase. The Android app never talks to R2/Supabase directly.
 */
@Composable
fun ArtistUploadScreen() {
    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(text = "Upload your music")
            // TODO Phase 4: file picker -> multipart upload to backend -> R2 + Supabase catalog write
        }
    }
}
