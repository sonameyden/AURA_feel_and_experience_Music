package com.aura.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aura.app.navigation.AuraNavHost
import com.aura.core.designsystem.theme.AuraTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity for the whole app. Every screen is a Composable destination
 * inside AuraNavHost — no other Activities should be added.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuraApp()
        }
    }
}

@Composable
private fun AuraApp() {
    AuraTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            AuraNavHost(navController = navController)
        }
    }
}
