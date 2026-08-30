package com.aura.core.data.repository

import com.aura.core.common.util.AppDispatchers
import com.aura.core.data.local.dao.PlaylistDao
import com.aura.core.data.local.entity.PlaylistEntity
import com.aura.core.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val dispatchers: AppDispatchers
) {
    fun observeByOwner(ownerId: String): Flow<List<Playlist>> =
        playlistDao.observeByOwner(ownerId).map { entities -> entities.map { it.toDomain() } }

    suspend fun save(playlist: Playlist) = withContext(dispatchers.io) {
        playlistDao.upsert(PlaylistEntity.fromDomain(playlist))
    }
}
