package com.aura.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Single source of truth for Song data. 
 * Fields are marked with @Json to match the backend's camelCase visual model contract.
 */
@JsonClass(generateAdapter = true)
data class Song(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "artistId") val artistId: String = "unknown",
    @Json(name = "artistName") val artistName: String = "Unknown Artist",
    @Json(name = "albumId") val albumId: String? = null,
    @Json(name = "durationMs") val durationMs: Long = 0,
    @Json(name = "streamUrl") val streamUrl: String = "",
    @Json(name = "artworkUrl") val artworkUrl: String? = null,
    @Json(name = "genre") val genre: String? = null,
    @Json(name = "releaseDate") val releaseDate: String? = null,
    @Json(name = "energy") val energy: Float = 0.5f,
    @Json(name = "valence") val valence: Float = 0.5f
)
