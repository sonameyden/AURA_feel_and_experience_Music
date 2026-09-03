package com.aura.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

/**
 * Per Section 11 of the project spec: reduced motion, visual intensity tiers,
 * cat visibility. Logout added here per the auth flow — signs out of
 * FirebaseAuth via AuthRepository, then AuraNavHost's onLoggedOut callback
 * clears the back stack and routes to Login.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var reducedMotion by remember { mutableStateOf(false) }
    var catVisible by remember { mutableStateOf(true) } // on by default per spec

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(text = "Visual Experience")

            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(text = "Reduced motion")
                Switch(checked = reducedMotion, onCheckedChange = { reducedMotion = it })
            }

            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(text = "Cat companion")
                Switch(checked = catVisible, onCheckedChange = { catVisible = it })
            }

            // TODO Phase 5: visual intensity tier (Low/Medium/High) selector, particle/kaleidoscope intensity sliders

            Text(text = "Account", modifier = Modifier.padding(top = 32.dp))
            Button(
                onClick = {
                    viewModel.logout()
                    onLoggedOut()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Log out")
            }
        }
    }
}
