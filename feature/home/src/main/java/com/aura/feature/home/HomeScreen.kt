package com.aura.feature.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aura.core.designsystem.components.AlbumArt
import com.aura.core.designsystem.components.GlassCard
import com.aura.core.designsystem.components.PlayingGlowRing
import com.aura.core.model.Artist
import com.aura.core.model.Song
import com.aura.feature.library.AddToPlaylistDialog
import com.aura.feature.library.LibraryUiState
import com.aura.feature.library.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSongClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onMoodClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val libState by libraryViewModel.uiState.collectAsState()
    val currentPlayingSongId by viewModel.currentPlayingSongId.collectAsState()
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    
    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "AURA",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp,
                                color = if (isLight) Color(0xFF29262D) else Color.White
                            )
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onProfileClick,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isLight) Color.White.copy(alpha = 0.65f)
                                    else Color.White.copy(alpha = 0.1f)
                                )
                                .let { if (isLight) it.shadow(2.dp, CircleShape) else it }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = if (isLight) Color(0xFF514C56) else Color.White,
                                modifier = Modifier.size(24.dp)
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
                    .padding(top = padding.calculateTopPadding())
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Step into it.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        color = if (isLight) Color(0xFF77717A) else Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = "Discover by Mood",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLight) Color(0xFF29262D) else Color.White
                    )
                )
                
                MoodPortalCard(onClick = onMoodClick, isLight = isLight)

                when (val state = uiState) {
                    is HomeUiState.Ready -> {
                        SectionHeader(title = "Trending", isLight = isLight)
                        LazyRow(
                            contentPadding = PaddingValues(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.trending) { song ->
                                TrendingSongCard(
                                    song = song,
                                    isPlaying = song.id == currentPlayingSongId,
                                    isLight = isLight,
                                    onSongClick = {
                                        viewModel.onSongClick(song)
                                        onSongClick(song.id)
                                    },
                                    onArtistClick = { onArtistClick(song.artistId) },
                                    onMoreClick = { songToAddToPlaylist = song }
                                )
                            }
                        }

                        if (state.recommendedArtists.isNotEmpty()) {
                            SectionHeader(title = "Artists for you", isLight = isLight)
                            LazyRow(
                                contentPadding = PaddingValues(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                items(state.recommendedArtists) { artist ->
                                    ArtistCircleCard(
                                        artist = artist,
                                        isLight = isLight,
                                        onClick = { onArtistClick(artist.id) }
                                    )
                                }
                            }
                        }
                        
                        if (state.recommendations.isNotEmpty()) {
                            SectionHeader(title = "Recommended For You", isLight = isLight)
                            LazyRow(
                                contentPadding = PaddingValues(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.recommendations) { song ->
                                    TrendingSongCard(
                                        song = song,
                                        isPlaying = song.id == currentPlayingSongId,
                                        isLight = isLight,
                                        onSongClick = {
                                            viewModel.onSongClick(song)
                                            onSongClick(song.id)
                                        },
                                        onArtistClick = { onArtistClick(song.artistId) },
                                        onMoreClick = { songToAddToPlaylist = song }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(130.dp)) // Natural end of content, higher for miniplayer
                    }

                    is HomeUiState.Loading -> {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    is HomeUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
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

@Composable
private fun SectionHeader(title: String, isLight: Boolean) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            color = if (isLight) Color(0xFF29262D) else Color.White
        ),
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun MoodPortalCard(onClick: () -> Unit, isLight: Boolean) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .height(140.dp),
        onClick = onClick,
        accentColor = if (isLight) Color(0xFFEEE8F0) else Color(0xFFA79AC7).copy(alpha = 0.2f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Aura Orb Detail
            if (isLight) {
                AuraOrb(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 20.dp)
                        .size(160.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "How are you feeling?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (isLight) Color(0xFF29262D) else Color.White
                )
                Text(
                    text = "Let AI find the perfect world for you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLight) Color(0xFF77717A) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TrendingSongCard(
    song: Song,
    isPlaying: Boolean,
    isLight: Boolean,
    onSongClick: () -> Unit,
    onArtistClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .shadow(
                    elevation = if (isLight) 6.dp else 0.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(if (isLight) Color(0xFFF0ECE9) else Color.DarkGray)
                .clickable(onClick = onSongClick)
        ) {
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
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLight) Color(0xFF302D33) else Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artistName,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = if (isLight) Color(0xFF817A83) else Color.White.copy(alpha = 0.6f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(onClick = onArtistClick)
                )
            }
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = if (isLight) Color(0xFF817A83) else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ArtistCircleCard(
    artist: Artist,
    isLight: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlbumArt(
            url = artist.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            shape = CircleShape
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist.name,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isLight) Color(0xFF302D33) else Color.White
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AuraOrb(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFDDD4E8).copy(alpha = 0.6f),
                        Color(0xFFEAD9DD).copy(alpha = 0.4f),
                        Color.Transparent
                    )
                )
            )
    )
}
