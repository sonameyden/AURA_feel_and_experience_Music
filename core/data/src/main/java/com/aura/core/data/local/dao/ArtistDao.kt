package com.aura.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aura.core.data.local.entity.ArtistEntity

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists WHERE id = :artistId")
    suspend fun getById(artistId: String): ArtistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(artists: List<ArtistEntity>)
}
