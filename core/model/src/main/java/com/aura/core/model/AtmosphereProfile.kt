package com.aura.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AtmosphereProfile(
    @Json(name = "song_id") val songId: String,
    @Json(name = "emotion") val emotion: String = "neutral",
    @Json(name = "energy") val energy: Float = 0.5f,
    @Json(name = "valence") val valence: Float = 0.5f,
    @Json(name = "environment") val environment: EnvironmentType = EnvironmentType.Nature,
    @Json(name = "primary_color_hex") val primaryColorHex: String = "#A79AC7",
    @Json(name = "secondary_color_hexes") val secondaryColorHexes: List<String> = emptyList(),
    @Json(name = "lighting_style") val lightingStyle: LightingStyle = LightingStyle.Soft,
    @Json(name = "particle_style") val particleStyle: ParticleStyle = ParticleStyle.None,
    @Json(name = "kaleidoscope_style") val kaleidoscopeStyle: KaleidoscopeStyle = KaleidoscopeStyle.SoftOrganic,
    @Json(name = "cat_behavior") val catBehavior: CatBehaviorState = CatBehaviorState.Idle,
    @Json(name = "transition_style") val transitionStyle: TransitionStyle = TransitionStyle.Gentle,
    @Json(name = "resonant_lyric_line_ids") val resonantLyricLineIds: List<String> = emptyList(),
    @Json(name = "section_profiles") val sectionProfiles: List<SongSectionProfile> = emptyList()
) {
    companion object {
        fun loadingPlaceholder(songId: String): AtmosphereProfile = AtmosphereProfile(
            songId = songId,
            emotion = "neutral",
            energy = 0.3f,
            valence = 0.5f,
            environment = EnvironmentType.Nature,
            primaryColorHex = "#A79AC7",
            secondaryColorHexes = listOf("#E9E4DE"),
            lightingStyle = LightingStyle.Soft,
            particleStyle = ParticleStyle.None,
            kaleidoscopeStyle = KaleidoscopeStyle.SoftOrganic,
            catBehavior = CatBehaviorState.Idle,
            transitionStyle = TransitionStyle.Gentle
        )
    }
}
