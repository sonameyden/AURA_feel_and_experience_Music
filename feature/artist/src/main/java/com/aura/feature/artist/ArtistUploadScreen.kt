package com.aura.feature.artist

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aura.core.designsystem.components.AlbumArt
import com.aura.core.designsystem.components.GlassCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistUploadScreen(
    onBackClick: () -> Unit,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BackHandler(onBack = onBackClick)
    
    var title by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var isProcessingAudio by remember { mutableStateOf(false) }
    var selectedAudioFile by remember { mutableStateOf<File?>(null) }
    var selectedArtworkFile by remember { mutableStateOf<File?>(null) }
    var artworkUri by remember { mutableStateOf<Uri?>(null) }
    var durationMs by remember { mutableLongStateOf(0L) }
    
    val uploadState by viewModel.uploadState.collectAsState()

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isProcessingAudio = true
                try {
                    selectedAudioFile = getFileFromUri(context, it, "audio")
                    durationMs = getDurationFromUri(context, it)
                } finally {
                    isProcessingAudio = false
                }
            }
        }
    }

    val artworkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            artworkUri = it
            scope.launch {
                selectedArtworkFile = getFileFromUri(context, it, "artwork")
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Upload Music", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (uploadState is UploadState.Success) {
                UploadSuccessView(onBackClick)
            } else {
                UploadForm(
                    title = title,
                    onTitleChange = { title = it },
                    artistName = artistName,
                    onArtistNameChange = { artistName = it },
                    genre = genre,
                    onGenreChange = { genre = it },
                    selectedAudioFile = selectedAudioFile,
                    onAudioSelect = { if (!isProcessingAudio) audioPickerLauncher.launch("audio/*") },
                    artworkUri = artworkUri,
                    onArtworkSelect = { artworkPickerLauncher.launch("image/*") },
                    isProcessingAudio = isProcessingAudio,
                    uploadState = uploadState,
                    onUploadClick = {
                        selectedAudioFile?.let { audio ->
                            viewModel.uploadSong(title, artistName, genre, durationMs, audio, selectedArtworkFile) 
                        }
                    }
                )
            }
            
            // Extra padding for mini-player
            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}

@Composable
private fun UploadForm(
    title: String,
    onTitleChange: (String) -> Unit,
    artistName: String,
    onArtistNameChange: (String) -> Unit,
    genre: String,
    onGenreChange: (String) -> Unit,
    selectedAudioFile: File?,
    onAudioSelect: () -> Unit,
    artworkUri: Uri?,
    onArtworkSelect: () -> Unit,
    isProcessingAudio: Boolean,
    uploadState: UploadState,
    onUploadClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Artwork Picker
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onArtworkSelect() },
            contentAlignment = Alignment.Center
        ) {
            if (artworkUri != null) {
                AlbumArt(
                    url = artworkUri.toString(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Select Artwork",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Column {
            Text(
                text = "Track Artwork",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Square images work best",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UploadTextField(
                value = title,
                onValueChange = onTitleChange,
                label = "Song Title",
                placeholder = "e.g. Moonlight Sonata"
            )

            UploadTextField(
                value = artistName,
                onValueChange = onArtistNameChange,
                label = "Artist Name",
                placeholder = "e.g. your artist name"
            )

            UploadTextField(
                value = genre,
                onValueChange = onGenreChange,
                label = "Genre",
                placeholder = "e.g. Ambient, Lo-Fi"
            )
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onAudioSelect
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isProcessingAudio) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AudioFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = if (isProcessingAudio) "Processing audio..." else selectedAudioFile?.name ?: "Select Audio File",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Supports WAV, MP3, FLAC (Max 50MB)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }

    AnimatedVisibility(visible = uploadState is UploadState.Error) {
        Text(
            text = (uploadState as? UploadState.Error)?.message ?: "Upload failed",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }

    Button(
        onClick = onUploadClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ),
        enabled = title.isNotBlank() && artistName.isNotBlank() && 
                 selectedAudioFile != null && uploadState !is UploadState.Loading && !isProcessingAudio
    ) {
        if (uploadState is UploadState.Loading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Start Upload", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun UploadSuccessView(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Upload Successful!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Your music and artwork are being processed and will be available soon.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
        )
        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
private fun UploadTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
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

private suspend fun getDurationFromUri(context: Context, uri: Uri): Long = withContext(Dispatchers.IO) {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, uri)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
    } catch (e: Exception) {
        0L
    } finally {
        retriever.release()
    }
}
