package com.aura.feature.nowplaying.visuals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import com.aura.core.model.CatBehaviorState

private const val CAT_ASSET_PATH = "cat_companion.riv" // place at feature/nowplaying/src/main/assets/cat_companion.riv

/**
 * The persistent cat companion. Loads assets/cat_companion.riv if present —
 * ONE file, reused across every environment (not one cat per environment,
 * per the "single consistent character" decision). Falls back to a plain
 * circle placeholder if the file doesn't exist yet.
 *
 * NOT a user-customizable/gamified avatar — its state is driven entirely by
 * the current AtmosphereProfile.catBehavior + live audio energy, never by
 * direct user input.
 *
 * State machine input convention (set this up in the Rive editor): a
 * "energy" number input (0..1), and a trigger/enum input named
 * "behaviorState" matching the CatBehaviorState names (Idle, Sleeping,
 * Walking, Stretching, Playing, Running, Watching).
 */
@Composable
fun CatCompanion(
    behaviorState: CatBehaviorState,
    liveAudioEnergy: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assetExists = remember { assetFileExistsCat(context) }

    if (!assetExists) {
        Box(
            modifier = modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Intentionally empty — placeholder only.
        }
        return
    }

    val currentEnergy by rememberUpdatedState(liveAudioEnergy)
    var riveView by remember { mutableStateOf<RiveAnimationView?>(null) }

    AndroidView(
        modifier = modifier.size(96.dp),
        factory = { ctx ->
            RiveInitializer.ensureInitialized(ctx)
            RiveAnimationView(ctx).also { view -> riveView = view }
        },
        update = { view ->
            runCatching { view.setNumberState("StateMachine", "energy", currentEnergy) }
        }
    )

    LaunchedEffect(riveView) {
        // NOTE: verify setRiveBytes' exact signature against the pinned
        // rive-android version — see the matching note in EnvironmentBackground.kt.
        riveView?.let { view ->
            runCatching {
                context.assets.open(CAT_ASSET_PATH).use { stream ->
                    view.setRiveBytes(
                        bytes = stream.readBytes(),
                        stateMachineName = "StateMachine",
                        autoplay = true
                    )
                }
            }
        }
    }

    // Fires the behavior trigger every time the AtmosphereProfile's cat
    // behavior changes (e.g. a song section changes what the cat should do).
    LaunchedEffect(riveView, behaviorState) {
        riveView?.let { view ->
            runCatching {
                view.fireState("StateMachine", behaviorState.name)
            }
        }
    }
}

private fun assetFileExistsCat(context: android.content.Context): Boolean = runCatching {
    context.assets.open(CAT_ASSET_PATH).close()
    true
}.getOrDefault(false)
