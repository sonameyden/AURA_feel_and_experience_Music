package com.aura.feature.nowplaying.visuals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.core.data.remote.LyricLine

/**
 * Synced lyrics with resonant-line highlighting (Section 26-28 of the original
 * concept). Readability comes first per the spec — no environment-specific
 * floating/reflective text styling in this Phase 1 version, add that once the
 * base version reads correctly.
 */
@Composable
fun LyricsOverlay(
    currentLine: LyricLine?,
    isResonant: Boolean,
    modifier: Modifier = Modifier
) {
    if (currentLine == null) return

    Column(modifier = modifier.fillMaxWidth().padding(24.dp)) {
        Text(
            text = currentLine.text,
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = if (isResonant) 22.sp else 18.sp,
            fontWeight = if (isResonant) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
