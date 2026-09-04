package com.aura.core.data.repository

import com.aura.core.common.util.AppDispatchers
import com.aura.core.common.util.AppError
import com.aura.core.common.util.AppResult
import com.aura.core.data.local.dao.CatalogCacheDao
import com.aura.core.data.local.dao.SongDao
import com.aura.core.data.local.entity.CatalogCacheEntity
import com.aura.core.data.local.entity.SongEntity
import com.aura.core.data.remote.CatalogApi
import com.aura.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
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
    private val catalogCacheDao: CatalogCacheDao,
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
        runCatching {
            // 1. Try to return cache immediately
            val cached = catalogCacheDao.getBySection("trending")

            // 2. Refresh from network
            val remote = catalogApi.getTrending()
            songDao.upsertAll(remote.map { SongEntity.fromDomain(it) })
            catalogCacheDao.upsert(CatalogCacheEntity("trending", remote.map { it.id }))

            remote
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = {
                // If network fails, try to fallback to cache if we haven't already returned it
                val fallback = catalogCacheDao.getBySection("trending")?.songIds
                    ?.mapNotNull { songDao.getById(it)?.toDomain() }
                if (!fallback.isNullOrEmpty()) AppResult.Success(fallback)
                else AppResult.Error(it.toAppError())
            }
        )
    }

    suspend fun getRecommendations(basedOnSongId: String? = null): AppResult<List<Song>> =
        withContext(dispatchers.io) {
            val sectionKey = if (basedOnSongId == null) "recommended_for_you" else "similar_to_$basedOnSongId"
            runCatching {
                val remote = catalogApi.getRecommendations(basedOnSongId)
                songDao.upsertAll(remote.map { SongEntity.fromDomain(it) })
                catalogCacheDao.upsert(CatalogCacheEntity(sectionKey, remote.map { it.id }))
                remote
            }.fold(
                onSuccess = { AppResult.Success(it) },
                onFailure = {
                    val fallback = catalogCacheDao.getBySection(sectionKey)?.songIds
                        ?.mapNotNull { songDao.getById(it)?.toDomain() }
                    if (!fallback.isNullOrEmpty()) AppResult.Success(fallback)
                    else AppResult.Error(it.toAppError())
                }
            )
        }

    suspend fun uploadSong(title: String, genre: String, file: File): AppResult<Song> =
        withContext(dispatchers.io) {
            runCatching {
                val titlePart = title.toRequestBody("text/plain".toMediaType())
                val genrePart = genre.toRequestBody("text/plain".toMediaType())
                val filePart = MultipartBody.Part.createFormData(
                    "audioFile",
                    file.name,
                    file.asRequestBody("audio/*".toMediaType())
                )
                val remote = catalogApi.uploadSong(titlePart, genrePart, filePart)
                songDao.upsert(SongEntity.fromDomain(remote))
                remote
            }.fold(
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
