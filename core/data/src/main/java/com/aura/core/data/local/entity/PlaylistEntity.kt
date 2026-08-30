package com.aura.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aura.core.data.local.RoomConverters
import com.aura.core.model.Playlist

@Entity(tableName = "playlists")
@TypeConverters(RoomConverters::class)
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val ownerId: String,
    val songIds: List<String>,
    val isPublic: Boolean,
    val coverColorHex: String?
) {
    fun toDomain(): Playlist = Playlist(id, title, ownerId, songIds, isPublic, coverColorHex)

    companion object {
        fun fromDomain(playlist: Playlist): PlaylistEntity = PlaylistEntity(
            playlist.id, playlist.title, playlist.ownerId,
            playlist.songIds, playlist.isPublic, playlist.coverColorHex
        )
    }
}
