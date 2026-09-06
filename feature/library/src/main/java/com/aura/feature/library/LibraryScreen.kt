package com.aura.feature.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aura.core.designsystem.components.GlassCard

@Composable
fun LibraryScreen(
    onPlaylistClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onLikedSongsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onPlaylistsClick: () -> Unit,
    onSongClick: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.load()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        when (val state = uiState) {
            is LibraryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is LibraryUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.load() }, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Retry")
                        }
                    }
                }
            }
            is LibraryUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Library",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isLight) Color(0xFF29262D) else Color.White
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    item {
                        LibraryItem(
                            title = "Liked Songs", 
                            icon = Icons.Default.Favorite, 
                            isLight = isLight,
                            onClick = onLikedSongsClick
                        )
                    }

                    item {
                        LibraryItem(
                            title = "Playlists", 
                            icon = Icons.Default.List, 
                            isLight = isLight,
                            onClick = onPlaylistsClick
                        )
                    }

                    item {
                        LibraryItem(
                            title = "History", 
                            icon = Icons.Default.History, 
                            isLight = isLight,
                            onClick = onHistoryClick
                        )
                    }

                    item {
                        LibraryItem(
                            title = "My Music Worlds", 
                            icon = Icons.Default.LibraryMusic, 
                            isLight = isLight,
                            onClick = {} // Placeholder for future feature
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryItem(title: String, icon: ImageVector, isLight: Boolean, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isLight) Color(0xFFA79AC7) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title, 
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isLight) Color(0xFF29262D) else Color.White
                )
            )
        }
    }
}
