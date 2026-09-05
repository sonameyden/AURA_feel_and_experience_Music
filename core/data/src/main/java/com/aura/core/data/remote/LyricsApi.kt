package com.aura.core.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Talks to YOUR backend's lyrics-lookup proxy (which itself calls Genius/Musixmatch
 * server-side and caches results — see Section 9 of the project spec). The Android
 * app never calls a third-party lyrics provider directly.
 */
interface LyricsApi {
    @GET("lyrics/{songId}")
    suspend fun getLyrics(@Path("songId") songId: String): LyricsResponse
}

@JsonClass(generateAdapter = true)
data class LyricsResponse(
    @Json(name = "song_id") val songId: String,
    @Json(name = "lines") val lines: List<LyricLine> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LyricLine(
    @Json(name = "id") val id: String,
    @Json(name = "text") val text: String = "",
    @Json(name = "start_time_ms") val startTimeMs: Long = 0,
    @Json(name = "end_time_ms") val endTimeMs: Long = 0
)
