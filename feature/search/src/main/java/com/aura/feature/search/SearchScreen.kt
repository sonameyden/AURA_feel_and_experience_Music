package com.aura.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aura.core.designsystem.components.GlassCard
import com.aura.core.model.Song
import com.aura.feature.library.AddToPlaylistDialog
import com.aura.feature.library.LibraryUiState
import com.aura.feature.library.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSongClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val libState by libraryViewModel.uiState.collectAsState()
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    
    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Search", 
                        color = if (isLight) Color(0xFF29262D) else Color.White,
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isLight) Color(0xFF29262D) else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.onQueryChanged(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                placeholder = { Text("Songs, artists, or worlds...", color = Color.Gray) },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = null, 
                        tint = if (isLight) Color(0xFFA79AC7) else Color.White.copy(alpha = 0.5f)
                    ) 
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (isLight) Color(0xFF29262D) else Color.White,
                    unfocusedTextColor = if (isLight) Color(0xFF29262D) else Color.White,
                    focusedBorderColor = Color(0xFFA79AC7),
                    unfocusedBorderColor = if (isLight) Color.Black.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.1f),
                    cursorColor = Color(0xFFA79AC7)
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            when (val state = uiState) {
                is SearchUiState.Results -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.songs) { song ->
                            GlassCard(
                                onClick = {
                                    viewModel.onSongClick(song)
                                    onSongClick(song.id)
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = song.title,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isLight) Color(0xFF29262D) else Color.White
                                        )
                                        Text(
                                            text = song.artistName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isLight) Color(0xFF77717A) else Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.clickable { onArtistClick(song.artistId) }
                                        )
                                    }
                                    IconButton(onClick = { songToAddToPlaylist = song }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = if (isLight) Color(0xFF77717A) else Color.White.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is SearchUiState.Loading -> Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                is SearchUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                SearchUiState.Idle -> {
                    Text(
                        "Search for your next AURA.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isLight) Color(0xFF77717A) else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 48.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        songToAddToPlaylist?.let { song ->
            val playlists = (libState as? LibraryUiState.Success)?.playlists ?: emptyList()
            AddToPlaylistDialog(
                playlists = playlists,
                onPlaylistSelected = { playlistId ->
                    libraryViewModel.addSongToPlaylist(playlistId, song.id)
                },
                onDismiss = { songToAddToPlaylist = null }
            )
        }
    }
}
