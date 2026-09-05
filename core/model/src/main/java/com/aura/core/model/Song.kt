package com.aura.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Single source of truth for Song data. 
 * Fields are marked with @Json to match the backend's snake_case naming.
 * Most metadata is optional to handle varied backend/database states gracefully.
 */
@JsonClass(generateAdapter = true)
data class Song(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "artist_id") val artistId: String = "unknown",
    @Json(name = "artist_name") val artistName: String = "Unknown Artist",
    @Json(name = "album_id") val albumId: String? = null,
    @Json(name = "duration_ms") val durationMs: Long = 0,
    @Json(name = "stream_url") val streamUrl: String = "",
    @Json(name = "artwork_url") val artworkUrl: String? = null,
    @Json(name = "genre") val genre: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null
)
