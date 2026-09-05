package com.aura.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Album(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "artist_id") val artistId: String,
    @Json(name = "artwork_url") val artworkUrl: String?,
    @Json(name = "song_ids") val songIds: List<String>,
    @Json(name = "release_date") val releaseDate: String?
)
