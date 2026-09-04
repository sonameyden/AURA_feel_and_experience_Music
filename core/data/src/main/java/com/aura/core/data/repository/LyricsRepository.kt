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
        runCatching { 
            val raw = lyricsApi.getLyrics(songId)
            // Filter out Genius metadata noise and prose-like book text
            val filteredLines = raw.lines.filter { line ->
                val text = line.text.lowercase()
                val isMetadata = text.contains("contributors") || 
                                text.contains("translations") ||
                                text.contains("lyrics") ||
                                text.contains("produced by") ||
                                text.endsWith("embed")
                
                // Prose check: lyrics lines are rarely this long or formatted with 
                // nested double quotes like book dialogue.
                val isProse = line.text.length > 100 || 
                             (line.text.contains("“") && line.text.contains("?”"))

                !isMetadata && !isProse
            }
            raw.copy(lines = filteredLines)
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAppError()) }
        )
    }
}
