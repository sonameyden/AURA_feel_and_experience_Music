package com.aura.core.data.repository

import com.aura.core.common.util.AppDispatchers
import com.aura.core.common.util.AppResult
import com.aura.core.data.remote.LyricsApi
import com.aura.core.data.remote.LyricsResponse
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepository @Inject constructor(
    private val lyricsApi: LyricsApi,
    private val dispatchers: AppDispatchers
) {
    suspend fun getLyrics(songId: String): AppResult<LyricsResponse> = withContext(dispatchers.io) {
        runCatching { lyricsApi.getLyrics(songId) }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }
}
