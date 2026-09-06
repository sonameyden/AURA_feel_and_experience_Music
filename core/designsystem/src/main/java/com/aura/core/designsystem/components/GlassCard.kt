package com.aura.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Soft "glass" surface used across AURA. In Light Mode, it feels like
 * translucent pearl glass with very soft depth.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    accentColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val base = MaterialTheme.colorScheme.surface
    
    val background = if (isLight) {
        // Light Mode: Ethereal translucent pearl
        accentColor?.let { Color.White.copy(alpha = 0.8f).compositeOver(it) } 
            ?: Color.White.copy(alpha = 0.65f)
    } else {
        // Dark Mode: Muted translucent violet - reduced alpha for better visibility of background art
        accentColor?.let { base.copy(alpha = 0.7f).compositeOver(it) } ?: base.copy(alpha = 0.55f)
    }

    val shape = RoundedCornerShape(cornerRadius)
    val shadowColor = if (isLight) Color(0xFF29262D).copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isLight) 4.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(background)
            .let { m -> if (onClick != null) m.clickable(onClick = onClick) else m }
    ) {
        content()
    }
}
