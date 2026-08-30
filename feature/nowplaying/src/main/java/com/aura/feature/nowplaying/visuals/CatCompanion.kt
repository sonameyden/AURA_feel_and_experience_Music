package com.aura.feature.nowplaying.visuals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aura.core.model.CatBehaviorState

/**
 * The persistent cat companion — normally rive_cat.riv driven by a state
 * machine (`energy` float input, `behaviorState` trigger). This is a
 * placeholder (a plain circle) until cat_companion.riv exists — see
 * AURA_Project_Specification.md Section 8 for the real integration pattern.
 *
 * NOT a user-customizable/gamified avatar — one consistent character,
 * its state is driven entirely by the current AtmosphereProfile + live energy.
 */
@Composable
fun CatCompanion(
    behaviorState: CatBehaviorState,
    liveAudioEnergy: Float,
    modifier: Modifier = Modifier
) {
    // TODO Phase 3: replace with RiveAnimationView("cat_companion.riv") and drive
    // its state machine inputs from `behaviorState` and `liveAudioEnergy`.

    LaunchedEffect(behaviorState) {
        // Placeholder hook point — real implementation triggers the Rive state
        // machine's behaviorState input here.
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .background(Color.White.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Intentionally empty — placeholder only.
    }
}
