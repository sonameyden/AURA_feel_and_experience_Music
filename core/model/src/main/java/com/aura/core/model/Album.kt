package com.aura.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Album(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "artistId") val artistId: String,
    @Json(name = "artworkUrl") val artworkUrl: String? = null,
    @Json(name = "songIds") val songIds: List<String> = emptyList(),
    @Json(name = "releaseDate") val releaseDate: String? = null
)
