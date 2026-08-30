package com.aura.feature.nowplaying.visuals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile

/**
 * The illustrated "scenery" layer — normally a Rive asset (Section 8 of the
 * project spec). Falls back to a flat color placeholder if the .riv file for
 * this environment doesn't exist yet, so the rest of the app is never blocked
 * on art being finished. Swap the placeholder branch out per-environment as
 * each real .riv file is ready — nothing else in NowPlayingScreen needs to change.
 */
@Composable
fun EnvironmentBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    modifier: Modifier = Modifier
) {
    // TODO Phase 3: once env_*.riv assets exist, branch here:
    // val assetPath = "environments/${profile.environment.riveAssetFileName()}"
    // RiveAnimationView(assetPath) { setNumberState("StateMachine", "energy", liveAudioEnergy) }

    val placeholderColor = runCatching {
        Color(android.graphics.Color.parseColor(profile.primaryColorHex))
    }.getOrDefault(Color.DarkGray)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(placeholderColor)
    )
}
