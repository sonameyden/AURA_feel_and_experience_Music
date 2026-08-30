package com.aura.core.model

data class Song(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumId: String?,
    val durationMs: Long,
    val streamUrl: String,       // R2-hosted URL — never a local device path
    val artworkUrl: String?,
    val genre: String?,
    val releaseDate: String?
)
