package com.aura.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aura.core.model.Artist

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val bio: String?,
    val imageUrl: String?,
    val isVerified: Boolean,
    val followerCount: Int
) {
    fun toDomain() = Artist(
        id = id,
        name = name,
        bio = bio,
        imageUrl = imageUrl,
        isVerified = isVerified,
        followerCount = followerCount
    )

    companion object {
        fun fromDomain(artist: Artist) = ArtistEntity(
            id = artist.id,
            name = artist.name,
            bio = artist.bio,
            imageUrl = artist.imageUrl,
            isVerified = artist.isVerified,
            followerCount = artist.followerCount
        )
    }
}
