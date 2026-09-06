package com.aura.core.data.remote

import com.aura.core.model.Playlist
import com.aura.core.model.Song
import retrofit2.http.*

/**
 * Talks to YOUR backend's authenticated library endpoints.
 * All these require a valid Firebase ID token (handled by AuthInterceptor).
 */
interface LibraryApi {
    @GET("library/liked")
    suspend fun getLikedSongs(): List<Song>

    @POST("library/liked/{songId}")
    @Headers("Content-Type: application/json")
    suspend fun likeSong(
        @Path("songId") songId: String,
        @Body body: Map<String, String>
    )

    @DELETE("library/liked/{songId}")
    suspend fun unlikeSong(@Path("songId") songId: String)

    @GET("library/history")
    suspend fun getHistory(): List<Song>

    @POST("library/history/{songId}")
    @Headers("Content-Type: application/json")
    suspend fun addToHistory(
        @Path("songId") songId: String,
        @Body body: Map<String, String>
    )

    @GET("library/playlists")
    suspend fun getUserPlaylists(): List<Playlist>

    @GET("library/playlists/{id}")
    suspend fun getPlaylist(@Path("id") id: String): Playlist

    @POST("library/playlists")
    @Headers("Content-Type: application/json")
    suspend fun createPlaylist(@Body body: Map<String, String>): Playlist

    @POST("library/playlists/{playlistId}/songs")
    @Headers("Content-Type: application/json")
    suspend fun addSongToPlaylist(
        @Path("playlistId") playlistId: String,
        @Body body: Map<String, String>
    )

    @DELETE("library/playlists/{playlistId}")
    suspend fun deletePlaylist(@Path("playlistId") playlistId: String)

    @GET("library/worlds")
    suspend fun getSavedWorlds(): List<Any> 

    @POST("library/worlds")
    suspend fun saveWorld(@Body world: Any)
}
