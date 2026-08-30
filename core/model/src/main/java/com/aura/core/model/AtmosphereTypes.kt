package com.aura.core.model

/** How the environment's lighting layer should behave for the current moment in the song. */
enum class LightingStyle {
    Soft,
    Bright,
    Dramatic,
    Fading
}

/** Which particle asset/behavior the ParticleLayer (Compose Canvas) should render. */
enum class ParticleStyle {
    Dust,
    Rain,
    Bubbles,
    Petals,
    Stars,
    Sparks,
    None
}

/** Visual "flavor" of the kaleidoscope layer — drives shape style, not raw math params. */
enum class KaleidoscopeStyle {
    SoftOrganic,     // calm / heavenly
    FluidSymmetry,   // dreamy
    WaveLike,        // oceanic
    SharpGeometric,  // energetic
    DarkReflective,  // melancholic
    Floral           // romantic
}

/** How the world should morph when moving from the previous song's world into this one. */
enum class TransitionStyle {
    Gentle,      // e.g. rainy room -> heaven, 2-5s cross-fade of every layer
    Progressive, // e.g. hopeful songs that brighten through their own runtime
    Sharp        // e.g. into/out of Energetic, quicker cut with a flash/pulse
}

/** Song structure sections — used to drive per-section intensity overrides. */
enum class SongSection {
    Intro,
    Verse,
    PreChorus,
    Chorus,
    Bridge,
    FinalChorus,
    Outro
}

/**
 * Optional override applied during a specific window of the song's playback,
 * e.g. "boost intensity 1.4x during the Chorus starting at 42s."
 */
data class SongSectionProfile(
    val section: SongSection,
    val startTimeMs: Long,
    val intensityMultiplier: Float
)
