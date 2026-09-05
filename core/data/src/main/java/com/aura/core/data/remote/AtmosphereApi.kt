package com.aura.core.data.remote

import com.aura.core.model.AtmosphereProfile
import com.aura.core.model.Song
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
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

@JsonClass(generateAdapter = true)
data class MoodRequest(
    @Json(name = "moodText") val moodText: String,
    @Json(name = "recentlyPlayedSongIds") val recentlyPlayedSongIds: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MoodResponse(
    @Json(name = "mode") val mode: String, // "recommendations" or "direct_play"
    @Json(name = "reply") val reply: String,
    @Json(name = "songs") val songs: List<Song> = emptyList(),
    @Json(name = "song") val song: Song? = null,
    @Json(name = "atmosphereProfile") val atmosphereProfile: AtmosphereProfile? = null
)
