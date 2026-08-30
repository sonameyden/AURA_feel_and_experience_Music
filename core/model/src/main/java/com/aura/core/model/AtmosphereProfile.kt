package com.aura.core.model

/**
 * THE central data contract of the whole app. Every visual layer in
 * NowPlayingScreen (Rive environment, Rive cat, particles, kaleidoscope,
 * gradient, lyrics) reads from one instance of this class — nothing renders
 * anything about "what this song feels like" from anywhere else.
 *
 * Produced by AtmosphereRepository, which calls the backend described in
 * AURA_Project_Specification.md Section 9 (song genre/lyrics -> OpenAI ->
 * structured JSON -> this shape).
 */
data class AtmosphereProfile(
    val songId: String,
    val emotion: String,                       // e.g. "melancholic-comforting"
    val energy: Float,                          // 0f..1f — drives kaleidoscope speed, particle density, cat expressiveness
    val valence: Float,                         // 0f..1f — negative-to-positive mood
    val environment: EnvironmentType,
    val primaryColorHex: String,
    val secondaryColorHexes: List<String>,
    val lightingStyle: LightingStyle,
    val particleStyle: ParticleStyle,
    val kaleidoscopeStyle: KaleidoscopeStyle,
    val catBehavior: CatBehaviorState,
    val transitionStyle: TransitionStyle,
    val resonantLyricLineIds: List<String> = emptyList(), // set only when the user provided mood context
    val sectionProfiles: List<SongSectionProfile> = emptyList()
) {
    companion object {
        /**
         * Safe default used ONLY before the real profile has loaded (e.g. while
         * the backend call is in flight) or as a placeholder during Phase 1/2
         * development before Rive assets exist. Never shown as "the" atmosphere
         * for a song in production — always overwritten once the real call returns.
         */
        fun loadingPlaceholder(songId: String): AtmosphereProfile = AtmosphereProfile(
            songId = songId,
            emotion = "neutral",
            energy = 0.3f,
            valence = 0.5f,
            environment = EnvironmentType.Nature,
            primaryColorHex = "#A79AC7", // Aura Violet — the app's own neutral accent, not a "real" mood color
            secondaryColorHexes = listOf("#E9E4DE"),
            lightingStyle = LightingStyle.Soft,
            particleStyle = ParticleStyle.None,
            kaleidoscopeStyle = KaleidoscopeStyle.SoftOrganic,
            catBehavior = CatBehaviorState.Idle,
            transitionStyle = TransitionStyle.Gentle
        )
    }
}
