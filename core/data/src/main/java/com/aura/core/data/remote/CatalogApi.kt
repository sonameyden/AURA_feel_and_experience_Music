package com.aura.core.data.remote

import com.aura.core.model.Album
import com.aura.core.model.Artist
import com.aura.core.model.Playlist
import com.aura.core.model.Song
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Talks to YOUR backend's catalog endpoints (Section 10 of the project spec) —
 * never talks to Cloudflare R2 or Supabase directly from the client.
 */
interface CatalogApi {
    @GET("catalog/songs/{id}")
    suspend fun getSong(@Path("id") id: String): Song

    @GET("catalog/songs/search")
    suspend fun searchSongs(@Query("q") query: String): List<Song>

    @GET("catalog/artists/{id}")
    suspend fun getArtist(@Path("id") id: String): Artist

    @GET("catalog/songs/artist/{artistId}")
    suspend fun getSongsByArtist(@Path("artistId") artistId: String): List<Song>

    @GET("catalog/albums/{id}")
    suspend fun getAlbum(@Path("id") id: String): Album

    @GET("catalog/playlists/{id}")
    suspend fun getPlaylist(@Path("id") id: String): Playlist

    @GET("catalog/trending")
    suspend fun getTrending(): List<Song>

    @GET("catalog/recommendations")
    suspend fun getRecommendations(@Query("basedOn") songId: String? = null): List<Song>

    @Multipart
    @POST("catalog/songs/upload")
    suspend fun uploadSong(
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("artistName") artistName: RequestBody,
        @Part("genre") genre: RequestBody,
        @Part("durationMs") durationMs: RequestBody
    ): Song
}
