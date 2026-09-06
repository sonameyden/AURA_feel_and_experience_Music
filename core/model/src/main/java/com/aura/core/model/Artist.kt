package com.aura.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Artist(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String = "Unknown Artist",
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "isVerified") val isVerified: Boolean = false,
    @Json(name = "followerCount") val followerCount: Int = 0,
    @Json(name = "monthlyListeners") val monthlyListeners: Int = 0
)
