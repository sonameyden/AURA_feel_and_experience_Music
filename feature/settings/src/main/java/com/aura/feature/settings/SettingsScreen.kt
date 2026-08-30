package com.aura.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

/** Per Section 11 of the project spec: reduced motion, visual intensity tiers, cat visibility. */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    var reducedMotion by remember { mutableStateOf(false) }
    var catVisible by remember { mutableStateOf(true) } // on by default per spec

    Scaffold { padding ->
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
        }
    }
}
