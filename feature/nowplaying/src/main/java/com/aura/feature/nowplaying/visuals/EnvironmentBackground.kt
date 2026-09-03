package com.aura.feature.nowplaying.visuals

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import com.aura.core.model.AtmosphereProfile

/**
 * The illustrated "scenery" layer. Loads a Rive asset from
 * assets/environments/<env>.riv if present (see feature/nowplaying's
 * build.gradle.kts for the rive-android dependency, and
 * src/main/assets/environments/ for where files go). Falls back to a flat
 * color placeholder if the file for this environment doesn't exist yet —
 * this fallback stays in place permanently, it's not just a development
 * scaffold, since it also protects against a missing/corrupt asset at runtime.
 *
 * State machine input convention (must match what you set up in the Rive
 * editor for each environment file): a "energy" number input (0..1) and an
 * "intensity" number input (0..1) — every environment file shares these two
 * names so this composable can drive any of them identically.
 */
@Composable
fun EnvironmentBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assetPath = "environments/${profile.environment.riveAssetFileName()}"
    val assetExists = remember(assetPath) { assetFileExists(context, assetPath) }

    if (!assetExists) {
        val placeholderColor = runCatching {
            Color(android.graphics.Color.parseColor(profile.primaryColorHex))
        }.getOrDefault(Color.DarkGray)

        Box(modifier = modifier.fillMaxSize().background(placeholderColor))
        return
    }

    val currentEnergy by rememberUpdatedState(liveAudioEnergy)
    val currentIntensity by rememberUpdatedState(profile.energy)

    var riveView by remember(assetPath) { mutableStateOf<RiveAnimationView?>(null) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            RiveInitializer.ensureInitialized(ctx)
            // Plain, empty RiveAnimationView — actual asset bytes are loaded
            // below in LaunchedEffect via setRiveBytes(), once the view exists.
            RiveAnimationView(ctx).also { view -> riveView = view }
        },
        update = { view ->
            // Re-push the current live values every recomposition — cheap, and
            // keeps the environment in sync even across energy changes that
            // don't trigger a full asset reload.
            runCatching {
                view.setNumberState("StateMachine", "energy", currentEnergy)
                view.setNumberState("StateMachine", "intensity", currentIntensity)
            }
        }
    )

    LaunchedEffect(riveView, assetPath) {
        // Load the asset bytes from the module's assets/ folder into the view.
        //
        // NOTE: verify the exact method name/signature against the rive-android
        // version pinned in gradle/libs.versions.toml before relying on this —
        // Rive's Kotlin API has shifted between major versions (older versions
        // used setRiveBytes(...), some newer ones expose a File/Resource
        // builder instead). Check https://rive.app/docs (Android runtime docs)
        // for the exact call matching your pinned version if this doesn't compile.
        riveView?.let { view ->
            runCatching {
                context.assets.open(assetPath).use { stream ->
                    view.setRiveBytes(
                        bytes = stream.readBytes(),
                        stateMachineName = "StateMachine",
                        autoplay = true
                    )
                }
            }
        }
    }
}

private fun assetFileExists(context: Context, path: String): Boolean = runCatching {
    context.assets.open(path).close()
    true
}.getOrDefault(false)
