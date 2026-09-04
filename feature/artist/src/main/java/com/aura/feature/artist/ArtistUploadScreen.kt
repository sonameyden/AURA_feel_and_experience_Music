package com.aura.feature.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aura.core.designsystem.components.GlassCard
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistUploadScreen(
    onBackClick: () -> Unit,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    
    val uploadState by viewModel.uploadState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Upload Music", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (uploadState is UploadState.Success) {
                UploadSuccessView(onBackClick)
            } else {
                UploadForm(
                    title = title,
                    onTitleChange = { title = it },
                    genre = genre,
                    onGenreChange = { genre = it },
                    selectedFile = selectedFile,
                    onFileSelect = { selectedFile = File("simulated_track.mp3") },
                    uploadState = uploadState,
                    onUploadClick = {
                        selectedFile?.let { viewModel.uploadSong(title, genre, it) }
                    }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.UploadForm(
    title: String,
    onTitleChange: (String) -> Unit,
    genre: String,
    onGenreChange: (String) -> Unit,
    selectedFile: File?,
    onFileSelect: () -> Unit,
    uploadState: UploadState,
    onUploadClick: () -> Unit
) {
    Text(
        text = "Share your sound with the world.",
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 16.sp
    )

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
                value = genre,
                onValueChange = onGenreChange,
                label = "Genre",
                placeholder = "e.g. Ambient, Lo-Fi"
            )
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onFileSelect
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AudioFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = selectedFile?.name ?: "Select Audio File",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "Supports WAV, MP3, FLAC (Max 50MB)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
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

    Spacer(modifier = Modifier.weight(1f))

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
        enabled = title.isNotBlank() && selectedFile != null && uploadState !is UploadState.Loading
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
private fun ColumnScope.UploadSuccessView(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
            color = Color.White
        )
        Text(
            text = "Your music is being processed and will be available soon.",
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.7f),
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
            color = Color.White.copy(alpha = 0.6f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}
