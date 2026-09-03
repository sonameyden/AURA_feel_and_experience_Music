package com.aura.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aura.core.data.local.entity.CatalogCacheEntity

@Dao
interface CatalogCacheDao {
    @Query("SELECT * FROM catalog_cache WHERE sectionKey = :sectionKey")
    suspend fun getBySection(sectionKey: String): CatalogCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cache: CatalogCacheEntity)

    @Query("DELETE FROM catalog_cache WHERE sectionKey = :sectionKey")
    suspend fun deleteSection(sectionKey: String)
}
