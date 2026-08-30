package com.aura.core.data.repository

import com.aura.core.common.util.AppDispatchers
import com.aura.core.common.util.AppError
import com.aura.core.common.util.AppResult
import com.aura.core.data.local.dao.SongDao
import com.aura.core.data.local.entity.SongEntity
import com.aura.core.data.remote.CatalogApi
import com.aura.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for song/catalog data. UI/ViewModels never call
 * CatalogApi or SongDao directly — only this repository.
 *
 * Pattern: Room is read-through cache; network is source of truth; every
 * exception is converted into AppError at this boundary (never leaks raw
 * exceptions up to the ViewModel), per the best-practices doc.
 */
@Singleton
class SongRepository @Inject constructor(
    private val catalogApi: CatalogApi,
    private val songDao: SongDao,
    private val dispatchers: AppDispatchers
) {
    fun observeCachedSongs(): Flow<List<Song>> =
        songDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun getSong(songId: String): AppResult<Song> = withContext(dispatchers.io) {
        runCatching {
            val cached = songDao.getById(songId)
            cached?.toDomain() ?: run {
                val remote = catalogApi.getSong(songId)
                songDao.upsert(SongEntity.fromDomain(remote))
                remote
            }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }

    suspend fun search(query: String): AppResult<List<Song>> = withContext(dispatchers.io) {
        runCatching {
            val remote = catalogApi.searchSongs(query)
            songDao.upsertAll(remote.map { SongEntity.fromDomain(it) })
            remote
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }

    suspend fun getTrending(): AppResult<List<Song>> = withContext(dispatchers.io) {
        runCatching { catalogApi.getTrending() }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }

    suspend fun getRecommendations(basedOnSongId: String? = null): AppResult<List<Song>> =
        withContext(dispatchers.io) {
            runCatching { catalogApi.getRecommendations(basedOnSongId) }.fold(
                onSuccess = { AppResult.Success(it) },
                onFailure = { AppResult.Error(it.toAppError()) }
            )
        }
}

/** Shared exception -> AppError mapping, used across every repository in this module. */
internal fun Throwable.toAppError(): AppError = when (this) {
    is IOException -> AppError.Network
    else -> AppError.Unknown(message)
}
