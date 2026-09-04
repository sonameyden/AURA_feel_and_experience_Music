package com.aura.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

/**
 * The calm atmospheric backdrop shared by every non-immersive screen (Home,
 * Search, and any other screen that wants it). A restrained top-fade wash
 * of the brand accent over the neutral Aura background — atmosphere as a
 * hint, not a takeover. Now Playing does NOT use this; it has its own full
 * immersive engine (Rive + Canvas layers), which is deliberately much
 * stronger than this.
 */
@Composable
fun AuraBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                    ),
                    endY = 900f
                )
            )
    ) {
        content()
    }
}
