package com.aura.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Artist(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String = "Unknown Artist",
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    @Json(name = "follower_count") val followerCount: Int = 0,
    @Json(name = "monthly_listeners") val monthlyListeners: Int = 0
)
