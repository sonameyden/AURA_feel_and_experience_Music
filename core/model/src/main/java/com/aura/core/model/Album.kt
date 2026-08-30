package com.aura.core.model

data class Album(
    val id: String,
    val title: String,
    val artistId: String,
    val artworkUrl: String?,
    val songIds: List<String>,
    val releaseDate: String?
)
