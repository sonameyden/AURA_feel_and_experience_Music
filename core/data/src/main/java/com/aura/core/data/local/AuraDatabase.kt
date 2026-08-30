package com.aura.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aura.core.data.local.dao.PlaylistDao
import com.aura.core.data.local.dao.SongDao
import com.aura.core.data.local.entity.PlaylistEntity
import com.aura.core.data.local.entity.SongEntity

/**
 * Local cache database. Stores ONLY catalog metadata (Section 2 / 4 of the
 * project spec) — never audio bytes, those always stream from R2 via URL.
 */
@Database(
    entities = [SongEntity::class, PlaylistEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
}
