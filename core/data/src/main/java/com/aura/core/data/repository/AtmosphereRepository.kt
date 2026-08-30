package com.aura.core.data.repository

import com.aura.core.common.util.AppDispatchers
import com.aura.core.common.util.AppResult
import com.aura.core.data.remote.AtmosphereApi
import com.aura.core.data.remote.MoodRequest
import com.aura.core.data.remote.MoodResponse
import com.aura.core.model.AtmosphereProfile
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches/generates AtmosphereProfiles. This is the repository that ultimately
 * triggers the backend's OpenAI + lyrics-lookup flow (Section 9 of the project
 * spec) — this class itself never talks to OpenAI directly.
 */
@Singleton
class AtmosphereRepository @Inject constructor(
    private val atmosphereApi: AtmosphereApi,
    private val dispatchers: AppDispatchers
) {
    suspend fun getAtmosphereForSong(songId: String): AppResult<AtmosphereProfile> =
        withContext(dispatchers.io) {
            runCatching { atmosphereApi.getAtmosphereForSong(songId) }.fold(
                onSuccess = { AppResult.Success(it) },
                onFailure = { AppResult.Error(it.toAppError()) }
            )
        }

    suspend fun getAtmosphereForMood(moodText: String, recentlyPlayedSongIds: List<String> = emptyList()): AppResult<MoodResponse> =
        withContext(dispatchers.io) {
            runCatching {
                atmosphereApi.getAtmosphereForMood(MoodRequest(moodText, recentlyPlayedSongIds))
            }.fold(
                onSuccess = { AppResult.Success(it) },
                onFailure = { AppResult.Error(it.toAppError()) }
            )
        }
}
