package com.aura.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aura.core.model.Song

/**
 * Room-cached mirror of Song. Room NEVER stores audio bytes — only metadata
 * and the R2 streamUrl. This is purely a local cache of the server catalog,
 * not the source of truth (per Section 2 of the project spec).
 */
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumId: String?,
    val durationMs: Long,
    val streamUrl: String,
    val artworkUrl: String?,
    val genre: String?,
    val releaseDate: String?,
    val energy: Float,
    val valence: Float
) {
    fun toDomain(): Song = Song(
        id = id, title = title, artistId = artistId, artistName = artistName,
        albumId = albumId, durationMs = durationMs, streamUrl = streamUrl,
        artworkUrl = artworkUrl, genre = genre, releaseDate = releaseDate,
        energy = energy, valence = valence
    )

    companion object {
        fun fromDomain(song: Song): SongEntity = SongEntity(
            id = song.id, title = song.title, artistId = song.artistId,
            artistName = song.artistName, albumId = song.albumId,
            durationMs = song.durationMs, streamUrl = song.streamUrl,
            artworkUrl = song.artworkUrl, genre = song.genre, 
            releaseDate = song.releaseDate, energy = song.energy, 
            valence = song.valence
        )
    }
}
