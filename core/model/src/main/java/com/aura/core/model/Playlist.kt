package com.aura.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Playlist(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "owner_id") val ownerId: String,
    @Json(name = "song_ids") val songIds: List<String>,
    @Json(name = "is_public") val isPublic: Boolean,
    @Json(name = "cover_color_hex") val coverColorHex: String? = null
)
