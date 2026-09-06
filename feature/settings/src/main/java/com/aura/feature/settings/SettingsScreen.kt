package com.aura.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aura.core.designsystem.components.GlassCard
import com.aura.feature.artist.ArtistViewModel
import com.aura.feature.artist.UploadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLoggedOut: () -> Unit,
    onArtistProfileClick: (String) -> Unit,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    artistViewModel: ArtistViewModel = hiltViewModel()
) {
    var reducedMotion by remember { mutableStateOf(false) }
    var catVisible by remember { mutableStateOf(true) }
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    
    var showBecomeArtistDialog by remember { mutableStateOf(false) }
    val artistState by artistViewModel.uploadState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- Section 1: Visuals ---
            SettingsSection(title = "Visual Experience", icon = Icons.Default.Palette, isLight = isLight) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingToggle(
                            label = "Dark Mode",
                            checked = isDarkTheme,
                            onCheckedChange = onThemeChange,
                            isLight = isLight
                        )
                        HorizontalDivider(color = if (isLight) Color.Black.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                        SettingToggle(
                            label = "Reduced motion",
                            checked = reducedMotion,
                            onCheckedChange = { reducedMotion = it },
                            isLight = isLight
                        )
                        HorizontalDivider(color = if (isLight) Color.Black.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                        SettingToggle(
                            label = "Cat companion",
                            checked = catVisible,
                            onCheckedChange = { catVisible = it },
                            isLight = isLight
                        )
                    }
                }
            }

            // --- Section 2: Artist Features ---
            SettingsSection(title = "For Creators", icon = Icons.Default.Stars, isLight = isLight) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { 
                        onArtistProfileClick("me")
                    }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Artist Dashboard", 
                            fontWeight = FontWeight.Bold,
                            color = if (isLight) Color(0xFF29262D) else Color.White
                        )
                        Text(
                            text = "Manage your music and profile.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isLight) Color(0xFF77717A) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                if (artistState !is UploadState.Success) {
                    TextButton(
                        onClick = { showBecomeArtistDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Not an artist? Become one now")
                    }
                }
            }

            // --- Section 3: Account ---
            Button(
                onClick = {
                    viewModel.logout()
                    onLoggedOut()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLight) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    contentColor = if (isLight) Color(0xFFD32F2F) else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Log out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            // Extra padding for mini-player
            Spacer(modifier = Modifier.height(130.dp))
        }
    }

    if (showBecomeArtistDialog) {
        var artistName by remember { mutableStateOf("") }
        var artistBio by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showBecomeArtistDialog = false },
            title = { Text("Complete Your Artist Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = artistName,
                        onValueChange = { artistName = it },
                        label = { Text("Artist Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = artistBio,
                        onValueChange = { artistBio = it },
                        label = { Text("Bio (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        artistViewModel.becomeArtist(artistName, artistBio)
                        showBecomeArtistDialog = false
                    },
                    enabled = artistName.isNotBlank()
                ) {
                    if (artistState is UploadState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Finish")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBecomeArtistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    isLight: Boolean,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isLight) Color(0xFFA79AC7) else MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp),
                color = if (isLight) Color(0xFF77717A) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
        content()
    }
}

@Composable
private fun SettingToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLight: Boolean
) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label, 
            fontWeight = FontWeight.Medium,
            color = if (isLight) Color(0xFF29262D) else Color.White
        )
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFA79AC7),
                uncheckedThumbColor = if (isLight) Color.White else Color.Gray,
                uncheckedTrackColor = if (isLight) Color.Black.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.1f)
            )
        )
    }
}
