package com.aura.core.data.repository

import com.aura.core.common.util.AppDispatchers
import com.aura.core.common.util.AppResult
import com.aura.core.data.local.dao.PlaylistDao
import com.aura.core.data.local.entity.PlaylistEntity
import com.aura.core.data.remote.LibraryApi
import com.aura.core.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val libraryApi: LibraryApi,
    private val playlistDao: PlaylistDao,
    private val dispatchers: AppDispatchers
) {
    fun observeByOwner(ownerId: String): Flow<List<Playlist>> =
        playlistDao.observeByOwner(ownerId).map { entities -> entities.map { it.toDomain() } }

    suspend fun getUserPlaylists(): AppResult<List<Playlist>> = withContext(dispatchers.io) {
        runCatching {
            val remote = libraryApi.getUserPlaylists()
            println("PlaylistRepository: Fetched ${remote.size} playlists from server: $remote")
            playlistDao.upsertAll(remote.map { PlaylistEntity.fromDomain(it) })
            remote
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { 
                println("PlaylistRepository: Failed to fetch playlists: ${it.message}")
                it.printStackTrace()
                AppResult.Error(it.toAppError()) 
            }
        )
    }

    suspend fun getPlaylist(id: String): AppResult<Playlist> = withContext(dispatchers.io) {
        runCatching {
            val remote = libraryApi.getPlaylist(id)
            playlistDao.upsert(PlaylistEntity.fromDomain(remote))
            remote
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }

    suspend fun createPlaylist(title: String): AppResult<Playlist> = withContext(dispatchers.io) {
        runCatching {
            val remote = libraryApi.createPlaylist(mapOf("title" to title))
            playlistDao.upsert(PlaylistEntity.fromDomain(remote))
            remote
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: String): AppResult<Unit> = withContext(dispatchers.io) {
        runCatching {
            libraryApi.addSongToPlaylist(playlistId, mapOf("songId" to songId))
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }

    suspend fun deletePlaylist(playlistId: String): AppResult<Unit> = withContext(dispatchers.io) {
        runCatching {
            libraryApi.deletePlaylist(playlistId)
            playlistDao.deleteById(playlistId)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }

    suspend fun save(playlist: Playlist) = withContext(dispatchers.io) {
        playlistDao.upsert(PlaylistEntity.fromDomain(playlist))
    }
}
