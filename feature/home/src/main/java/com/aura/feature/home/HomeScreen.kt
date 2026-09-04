package com.aura.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aura.core.designsystem.components.AlbumArt
import com.aura.core.designsystem.components.AuraBackground
import com.aura.core.designsystem.components.GlassCard
import com.aura.core.designsystem.components.PlayingGlowRing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSongClick: (String) -> Unit,
    onMoodClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPlayingSongId by viewModel.currentPlayingSongId.collectAsState()

    AuraBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "AURA", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        IconButton(onClick = onProfileClick) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding: PaddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Step into it.", style = MaterialTheme.typography.bodyMedium)


                Text(
                    text = "Discover by Mood",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 24.dp)
                )
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    onClick = onMoodClick,
                    accentColor = Color(0xFFA79AC7) // Aura Violet
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "How are you feeling?",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Let AI find the perfect world for you.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                when (val state = uiState) {
                    is HomeUiState.Ready -> {
                        Text(
                            text = "Trending",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(top = 12.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.trending) { song ->
                                val isPlaying = song.id == currentPlayingSongId
                                GlassCard(
                                    modifier = Modifier.width(160.dp),
                                    onClick = {
                                        viewModel.onSongClick(song)
                                        onSongClick(song.id)
                                    },
                                    accentColor = if (isPlaying) MaterialTheme.colorScheme.primary else null
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Box(modifier = Modifier.size(136.dp)) {
                                            AlbumArt(
                                                url = song.artworkUrl,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            if (isPlaying) {
                                                PlayingGlowRing(
                                                    isPlaying = true,
                                                    accent = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = song.title,
                                            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = song.artistName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                    }

                    is HomeUiState.Loading -> Text(
                        text = "Loading...",
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    is HomeUiState.Error -> Text(
                        text = state.message,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}
