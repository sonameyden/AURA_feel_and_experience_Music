package com.aura.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

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
    val isDark = !MaterialTheme.colorScheme.surface.isLight()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!isDark) {
            // Light Mode Premium Atmospheric Glows
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFEDE7F2).copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = Offset(0f, 0f),
                            radius = 1200f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFF3E8E4).copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            center = Offset(1000f, 800f),
                            radius = 1000f
                        )
                    )
            )
        } else {
            // Dark Mode Standard Wash
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                            ),
                            endY = 900f
                        )
                    )
            )
        }
        content()
    }
}

private fun Color.isLight(): Boolean {
    val luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue
    return luminance > 0.5
}
