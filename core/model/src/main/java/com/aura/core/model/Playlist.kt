package com.aura.core.model

data class Playlist(
    val id: String,
    val title: String,
    val ownerId: String,
    val songIds: List<String>,
    val isPublic: Boolean,
    val coverColorHex: String? = null // optional aggregate-mood tint, derived from its songs' AtmosphereProfiles
)
