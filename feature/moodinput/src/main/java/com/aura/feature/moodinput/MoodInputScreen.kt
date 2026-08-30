package com.aura.feature.moodinput

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun MoodInputScreen(
    onSongSelected: (String) -> Unit,
    viewModel: MoodInputViewModel = hiltViewModel()
) {
    var text by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is MoodInputUiState.Matched) {
            onSongSelected(state.songId)
        }
    }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(text = "How are you feeling?")
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.padding(top = 12.dp)
            )
            Button(
                onClick = { viewModel.onSubmit(text) },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text("Find my music")
            }

            when (val state = uiState) {
                is MoodInputUiState.Loading -> Text("Finding something for you...")
                is MoodInputUiState.Error -> Text(state.message)
                else -> Unit
            }
        }
    }
}
