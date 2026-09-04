package com.aura.feature.nowplaying.visuals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.core.data.remote.LyricLine

/**
 * Synced lyrics with resonant-line highlighting.
 */
@Composable
fun LyricsOverlay(
    currentLine: LyricLine?,
    isResonant: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = currentLine != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier.fillMaxWidth()
    ) {
        if (currentLine != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text(
                    text = currentLine.text,
                    color = Color.White,
                    fontSize = if (isResonant) 26.sp else 20.sp,
                    fontWeight = if (isResonant) FontWeight.ExtraBold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
