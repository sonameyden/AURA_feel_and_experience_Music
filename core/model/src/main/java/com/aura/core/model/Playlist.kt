package com.aura.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Playlist(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "ownerId") val ownerId: String,
    @Json(name = "songIds") val songIds: List<String>,
    @Json(name = "isPublic") val isPublic: Boolean,
    @Json(name = "coverColorHex") val coverColorHex: String? = null
)
