package com.aura.core.data.repository

import com.aura.core.common.util.AppDispatchers
import com.aura.core.common.util.AppResult
import com.aura.core.data.local.dao.ArtistDao
import com.aura.core.data.local.entity.ArtistEntity
import com.aura.core.data.remote.CatalogApi
import com.aura.core.model.Artist
import com.aura.core.model.Song
import kotlinx.coroutines.withContext
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
            val cached = artistDao.getById(artistId)
            cached?.toDomain() ?: run {
                val remote = catalogApi.getArtist(artistId)
                artistDao.upsert(ArtistEntity.fromDomain(remote))
                remote
            }
        }.fold(
            onSuccess = { AppResult.Success(it) },
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
