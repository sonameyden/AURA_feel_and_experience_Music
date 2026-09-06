package com.aura.core.data.repository

import com.aura.core.common.util.AppDispatchers
import com.aura.core.common.util.AppResult
import com.aura.core.data.local.dao.ArtistDao
import com.aura.core.data.local.entity.ArtistEntity
import com.aura.core.data.remote.CatalogApi
import com.aura.core.model.Artist
import com.aura.core.model.Song
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistRepository @Inject constructor(
    private val catalogApi: CatalogApi,
    private val artistDao: ArtistDao,
    private val dispatchers: AppDispatchers
) {
    suspend fun getArtist(artistId: String): AppResult<Artist> = withContext(dispatchers.io) {
        runCatching {
            if (artistId == "me") {
                catalogApi.getMyArtistProfile()
            } else {
                val cached = artistDao.getById(artistId)
                cached?.toDomain() ?: run {
                    val remote = catalogApi.getArtist(artistId)
                    artistDao.upsert(ArtistEntity.fromDomain(remote))
                    remote
                }
            }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }

    suspend fun getArtists(): AppResult<List<Artist>> = withContext(dispatchers.io) {
        runCatching {
            val remote = catalogApi.getArtists()
            artistDao.upsertAll(remote.map { ArtistEntity.fromDomain(it) })
            remote
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }

    suspend fun createArtistProfile(name: String, bio: String = ""): AppResult<Artist> = withContext(dispatchers.io) {
        runCatching {
            catalogApi.createOrUpdateArtistProfile(mapOf("name" to name, "bio" to bio))
        }.fold(
            onSuccess = { 
                artistDao.upsert(ArtistEntity.fromDomain(it))
                AppResult.Success(it) 
            },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }

    suspend fun uploadProfileImage(file: File): AppResult<Artist> = withContext(dispatchers.io) {
        runCatching {
            val mediaType = "image/*".toMediaType()
            val requestFile = file.asRequestBody(mediaType)
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            catalogApi.uploadProfileImage(body)
        }.fold(
            onSuccess = {
                artistDao.upsert(ArtistEntity.fromDomain(it))
                AppResult.Success(it)
            },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }

    suspend fun getSongsByArtist(artistId: String): AppResult<List<Song>> = withContext(dispatchers.io) {
        runCatching {
            catalogApi.getSongsByArtist(artistId)
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }
}
