package com.aura.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aura.core.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getById(songId: String): SongEntity?

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun observeAll(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artistName LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(song: SongEntity)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteById(songId: String)
}
