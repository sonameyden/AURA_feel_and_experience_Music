package com.aura.core.data.remote

import com.aura.core.model.AtmosphereProfile
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Talks to YOUR backend's atmosphere endpoints. The backend is what actually
 * calls OpenAI (Section 9 of the project spec) — the API key never touches
 * this Android app.
 */
interface AtmosphereApi {

    /** Generate/fetch the AtmosphereProfile for a specific song (cached server-side after first call). */
    @GET("atmosphere/song/{songId}")
    suspend fun getAtmosphereForSong(@Path("songId") songId: String): AtmosphereProfile

    /** User typed a mood ("I feel lonely tonight") — returns matched song + its AtmosphereProfile. */
    @POST("atmosphere/mood")
    suspend fun getAtmosphereForMood(@Body request: MoodRequest): MoodResponse
}

data class MoodRequest(
    val moodText: String,
    val recentlyPlayedSongIds: List<String> = emptyList()
)

data class MoodResponse(
    val songId: String,
    val atmosphereProfile: AtmosphereProfile
)
