package com.aura.core.data.repository

import com.aura.core.common.util.AppDispatchers
import com.aura.core.common.util.AppResult
import com.aura.core.data.local.dao.SongDao
import com.aura.core.data.local.entity.SongEntity
import com.aura.core.data.remote.LibraryApi
import com.aura.core.model.Song
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    private val libraryApi: LibraryApi,
    private val songDao: SongDao,
    private val dispatchers: AppDispatchers
) {
    suspend fun getLikedSongs(): AppResult<List<Song>> = withContext(dispatchers.io) {
        runCatching {
            val remote = libraryApi.getLikedSongs()
            songDao.upsertAll(remote.map { SongEntity.fromDomain(it) })
            remote
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { 
                System.err.println("LibraryRepository: getLikedSongs failed: ${it.message}")
                AppResult.Error(it.toAppError()) 
            }
        )
    }

    suspend fun likeSong(songId: String): AppResult<Unit> = withContext(dispatchers.io) {
        runCatching {
            libraryApi.likeSong(songId, emptyMap())
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { 
                System.err.println("LibraryRepository: likeSong failed: ${it.message}")
                AppResult.Error(it.toAppError()) 
            }
        )
    }

    suspend fun unlikeSong(songId: String): AppResult<Unit> = withContext(dispatchers.io) {
        runCatching {
            libraryApi.unlikeSong(songId)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { 
                System.err.println("LibraryRepository: unlikeSong failed: ${it.message}")
                AppResult.Error(it.toAppError()) 
            }
        )
    }

    suspend fun getHistory(): AppResult<List<Song>> = withContext(dispatchers.io) {
        runCatching {
            val remote = libraryApi.getHistory()
            songDao.upsertAll(remote.map { SongEntity.fromDomain(it) })
            remote
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { 
                System.err.println("LibraryRepository: getHistory failed: ${it.message}")
                AppResult.Error(it.toAppError()) 
            }
        )
    }

    suspend fun addToHistory(songId: String): AppResult<Unit> = withContext(dispatchers.io) {
        runCatching {
            libraryApi.addToHistory(songId, emptyMap())
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { 
                System.err.println("LibraryRepository: addToHistory failed: ${it.message}")
                AppResult.Error(it.toAppError()) 
            }
        )
    }
}
