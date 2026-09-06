package com.aura.feature.artist

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aura.core.designsystem.components.AlbumArt
import com.aura.core.designsystem.components.GlassCard
import com.aura.core.model.Artist
import com.aura.core.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistProfileScreen(
    artistId: String,
    onBackClick: () -> Unit,
    onUploadClick: () -> Unit,
    onSongClick: (String) -> Unit,
    currentUserId: String? = null,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profileState by viewModel.profileState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    LaunchedEffect(artistId) {
        viewModel.loadArtistProfile(artistId)
    }

    val profileImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val file = getFileFromUri(context, it, "profile")
                viewModel.uploadProfileImage(file)
            }
        }
    }

    var selectedSongForArtwork by remember { mutableStateOf<String?>(null) }
    val songArtworkPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val file = getFileFromUri(context, it, "song_art")
                val songId = selectedSongForArtwork
                if (songId != null) {
                    viewModel.updateSongArtwork(songId, file)
                }
                selectedSongForArtwork = null
            }
        }
    }

    var songToDelete by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        when (val state = profileState) {
            is ArtistProfileState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is ArtistProfileState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ArtistProfileState.Success -> {
                val artist = state.artist
                val songs = state.songs
                val isOwnProfile = artist.id == currentUserId || artistId == "me" || artistId == "current_user"
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        ArtistHeader(
                            artist = artist,
                            isOwnProfile = isOwnProfile,
                            isUploadingImage = uploadState is UploadState.Loading,
                            onImageClick = { profileImagePicker.launch("image/*") }
                        )
                    }

                    item {
                        Text(
                            text = "Insights",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Monthly Listeners",
                                value = formatCount(artist.monthlyListeners),
                                icon = Icons.Default.People,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Atmosphere Likes",
                                value = "4.2K",
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (isOwnProfile) {
                        item {
                            Text(
                                text = "Content Management",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onUploadClick
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Column(modifier = Modifier.padding(start = 16.dp)) {
                                        Text(
                                            text = "Upload new experience",
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Add high-quality audio & sync lyrics",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Top Songs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    items(songs) { song ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSongClick(song.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(48.dp)) {
                                    AlbumArt(
                                        url = song.artworkUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (isOwnProfile) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.2f))
                                                .clickable { selectedSongForArtwork = song.id },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Edit Art",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                                    Text(
                                        text = song.title,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = song.artistName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                if (isOwnProfile) {
                                    IconButton(onClick = { songToDelete = song }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(130.dp))
                    }
                }
            }
        }
    }

    if (songToDelete != null) {
        AlertDialog(
            onDismissRequest = { songToDelete = null },
            title = { Text("Delete Track") },
            text = { Text("Are you sure you want to remove \"${songToDelete?.title}\"? This will delete both the audio and artwork files permanently.") },
            confirmButton = {
                Button(
                    onClick = {
                        val songId = songToDelete?.id
                        if (songId != null) {
                            viewModel.deleteSong(songId)
                        }
                        songToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { songToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (selectedSongForArtwork != null) {
        LaunchedEffect(selectedSongForArtwork) {
            songArtworkPicker.launch("image/*")
        }
    }
}

@Composable
private fun ArtistHeader(
    artist: Artist,
    isOwnProfile: Boolean,
    isUploadingImage: Boolean,
    onImageClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .then(if (isOwnProfile && !isUploadingImage) Modifier.clickable(onClick = onImageClick) else Modifier)
        ) {
            AlbumArt(
                url = artist.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape
            )
            
            if (isOwnProfile) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploadingImage) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Color.White)
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = artist.name,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (artist.isVerified) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp)
                )
            }
        }
        
        Text(
            text = "ID: ${artist.id}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", count / 1_000_000f)
        count >= 1_000 -> String.format(Locale.getDefault(), "%.1fK", count / 1_000f)
        else -> count.toString()
    }
}

private suspend fun getFileFromUri(context: Context, uri: Uri, prefix: String): File = withContext(Dispatchers.IO) {
    val extension = if (prefix == "audio") "mp3" else "jpg"
    val tempFile = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.$extension")
    context.contentResolver.openInputStream(uri)?.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    tempFile
}
