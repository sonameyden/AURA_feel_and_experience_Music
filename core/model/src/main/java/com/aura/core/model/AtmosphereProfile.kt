package com.aura.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AtmosphereProfile(
    @Json(name = "songId") val songId: String,
    @Json(name = "emotion") val emotion: String = "neutral",
    @Json(name = "energy") val energy: Float = 0.5f,
    @Json(name = "valence") val valence: Float = 0.5f,
    @Json(name = "environment") val environment: EnvironmentType = EnvironmentType.Nature,
    @Json(name = "primaryColorHex") val primaryColorHex: String = "#A79AC7",
    @Json(name = "secondaryColorHexes") val secondaryColorHexes: List<String> = emptyList(),
    @Json(name = "lightingStyle") val lightingStyle: LightingStyle = LightingStyle.Soft,
    @Json(name = "particleStyle") val particleStyle: ParticleStyle = ParticleStyle.None,
    @Json(name = "kaleidoscopeStyle") val kaleidoscopeStyle: KaleidoscopeStyle = KaleidoscopeStyle.SoftOrganic,
    @Json(name = "catBehavior") val catBehavior: CatBehaviorState = CatBehaviorState.Idle,
    @Json(name = "transitionStyle") val transitionStyle: TransitionStyle = TransitionStyle.Gentle,
    @Json(name = "resonantLyricLineIds") val resonantLyricLineIds: List<String> = emptyList(),
    @Json(name = "sectionProfiles") val sectionProfiles: List<SongSectionProfile> = emptyList()
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
