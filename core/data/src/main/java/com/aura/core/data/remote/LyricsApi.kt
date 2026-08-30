package com.aura.core.data.remote

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

data class LyricsResponse(
    val songId: String,
    val lines: List<LyricLine>
)

data class LyricLine(
    val id: String,
    val text: String,
    val startTimeMs: Long,
    val endTimeMs: Long
)
