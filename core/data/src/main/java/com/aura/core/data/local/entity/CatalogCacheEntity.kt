package com.aura.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores an ordered list of song IDs for a specific catalog section (e.g.
 * "trending", "recommended"). This allows the app to show a list immediately
 * from cache even if the network is slow/offline, then refresh once the API
 * returns.
 */
@Entity(tableName = "catalog_cache")
data class CatalogCacheEntity(
    @PrimaryKey val sectionKey: String, // "trending", "recommended_for_you", etc.
    val songIds: List<String>,
    val lastUpdatedMs: Long = System.currentTimeMillis()
)
