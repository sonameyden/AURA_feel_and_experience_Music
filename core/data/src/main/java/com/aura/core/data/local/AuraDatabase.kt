package com.aura.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aura.core.data.local.dao.ArtistDao
import com.aura.core.data.local.dao.CatalogCacheDao
import com.aura.core.data.local.dao.PlaylistDao
import com.aura.core.data.local.dao.SongDao
import com.aura.core.data.local.entity.ArtistEntity
import com.aura.core.data.local.entity.CatalogCacheEntity
import com.aura.core.data.local.entity.PlaylistEntity
import com.aura.core.data.local.entity.SongEntity

/**
 * Local cache database. Stores ONLY catalog metadata (Section 2 / 4 of the
 * project spec) — never audio bytes, those always stream from R2 via URL.
 */
@Database(
    entities = [SongEntity::class, PlaylistEntity::class, CatalogCacheEntity::class, ArtistEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun catalogCacheDao(): CatalogCacheDao
    abstract fun artistDao(): ArtistDao
}
