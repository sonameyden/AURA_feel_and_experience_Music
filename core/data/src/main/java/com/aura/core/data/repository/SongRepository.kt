package com.aura.core.data.repository

import android.util.Log
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
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for song/catalog data.
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
            val remote = catalogApi.getTrending()
            songDao.upsertAll(remote.map { SongEntity.fromDomain(it) })
            catalogCacheDao.upsert(CatalogCacheEntity("trending", remote.map { it.id }))
            remote
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = {
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

    suspend fun uploadSong(
        title: String,
        artistName: String,
        genre: String,
        durationMs: Long,
        audioFile: File,
        artworkFile: File?
    ): AppResult<Song> = withContext(dispatchers.io) {
        runCatching {
            val titlePart = title.toRequestBody("text/plain".toMediaType())
            val artistPart = artistName.toRequestBody("text/plain".toMediaType())
            val genrePart = genre.toRequestBody("text/plain".toMediaType())
            val durationPart = durationMs.toString().toRequestBody("text/plain".toMediaType())
            
            val audioPart = MultipartBody.Part.createFormData(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/*".toMediaType())
            )

            val artworkPart = artworkFile?.let {
                MultipartBody.Part.createFormData(
                    "artwork",
                    it.name,
                    it.asRequestBody("image/*".toMediaType())
                )
            }

            val remote = catalogApi.uploadSong(audioPart, artworkPart, titlePart, artistPart, genrePart, durationPart)
            songDao.upsert(SongEntity.fromDomain(remote))
            remote
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }

    suspend fun deleteSong(songId: String): AppResult<Unit> = withContext(dispatchers.io) {
        runCatching {
            Log.d("SongRepository", "Calling deleteSong API for id: $songId")
            catalogApi.deleteSong(songId)
            Log.d("SongRepository", "API delete success, removing from local DAO")
            songDao.deleteById(songId)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { 
                Log.e("SongRepository", "Delete failed: ${it.message}")
                it.printStackTrace()
                AppResult.Error(it.toAppError()) 
            }
        )
    }

    suspend fun updateSongArtwork(songId: String, artworkFile: File): AppResult<Song> = withContext(dispatchers.io) {
        runCatching {
            val artworkPart = MultipartBody.Part.createFormData(
                "artwork",
                artworkFile.name,
                artworkFile.asRequestBody("image/*".toMediaType())
            )
            val remote = catalogApi.updateSongArtwork(songId, artworkPart)
            songDao.upsert(SongEntity.fromDomain(remote))
            remote
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }
}

internal fun Throwable.toAppError(): AppError = when (this) {
    is IOException -> AppError.Network
    is HttpException -> {
        val msg = response()?.errorBody()?.string() ?: message()
        AppError.Unknown("HTTP ${code()}: $msg")
    }
    else -> AppError.Unknown(message)
}
