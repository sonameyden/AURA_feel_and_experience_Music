package com.aura.core.model

data class Artist(
    val id: String,
    val name: String,
    val bio: String?,
    val imageUrl: String?,
    val isVerified: Boolean,
    val followerCount: Int
)
