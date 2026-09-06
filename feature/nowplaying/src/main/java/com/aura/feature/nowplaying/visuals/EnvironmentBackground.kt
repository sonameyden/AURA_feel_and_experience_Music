package com.aura.feature.nowplaying.visuals

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aura.core.model.AtmosphereProfile
import com.aura.core.model.EnvironmentType
import com.aura.feature.nowplaying.visuals.environments.DreamBackground
import com.aura.feature.nowplaying.visuals.environments.EnergeticBackground
import com.aura.feature.nowplaying.visuals.environments.HeavenBackground
import com.aura.feature.nowplaying.visuals.environments.HopefulBackground
import com.aura.feature.nowplaying.visuals.environments.MelancholicBackground
import com.aura.feature.nowplaying.visuals.environments.NatureBackground
import com.aura.feature.nowplaying.visuals.environments.OceanBackground
import com.aura.feature.nowplaying.visuals.environments.RomanticBackground

/**
 * Replaces the old single-file procedural drawing (and, before that, the
 * placeholder-first Rive plan from Section 8 of the project spec -- per the
 * "no Rive" decision, this is Compose Canvas painting all the way down).
 *
 * This file is now ONLY a dispatcher: each environment's actual layered
 * painting (sky -> distant silhouette -> midground -> foreground -> resting
 * ledge / particles, matching the moodboard references) lives in its own
 * file under visuals/environments/, e.g. HeavenBackground.kt, OceanBackground.kt.
 * Swapping or redesigning one environment never requires touching this file
 * or any other environment's file -- same isolation guarantee the old
 * per-environment `.riv` asset mapping was meant to give.
 *
 * `beatPulse` is new here: previously only KaleidoscopeLayer reacted to beat
 * detection. Now the background itself gets a brief brightness bloom on
 * every detected beat (see EnvironmentCanvas in environments/EnvironmentCommon.kt).
 * NowPlayingScreen.kt needs to pass `beatPulse = state.beatPulse` at the call site.
 */
@Composable
fun EnvironmentBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    modifier: Modifier = Modifier,
    beatPulse: Boolean = false
) {
    when (profile.environment) {
        EnvironmentType.Heaven -> HeavenBackground(profile, liveAudioEnergy, beatPulse, modifier.fillMaxSize())
        EnvironmentType.Nature -> NatureBackground(profile, liveAudioEnergy, beatPulse, modifier.fillMaxSize())
        EnvironmentType.Ocean -> OceanBackground(profile, liveAudioEnergy, beatPulse, modifier.fillMaxSize())
        EnvironmentType.Dream -> DreamBackground(profile, liveAudioEnergy, beatPulse, modifier.fillMaxSize())
        EnvironmentType.Romantic -> RomanticBackground(profile, liveAudioEnergy, beatPulse, modifier.fillMaxSize())
        EnvironmentType.Melancholic -> MelancholicBackground(profile, liveAudioEnergy, beatPulse, modifier.fillMaxSize())
        EnvironmentType.Hopeful -> HopefulBackground(profile, liveAudioEnergy, beatPulse, modifier.fillMaxSize())
        EnvironmentType.Energetic -> EnergeticBackground(profile, liveAudioEnergy, beatPulse, modifier.fillMaxSize())
    }
}
