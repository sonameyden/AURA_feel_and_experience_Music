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
import retrofit2.http.Body
import retrofit2.http.DELETE

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

    @GET("catalog/artists")
    suspend fun getArtists(): List<Artist>

    @GET("catalog/artists/me")
    suspend fun getMyArtistProfile(): Artist

    @POST("catalog/artists/me")
    suspend fun createOrUpdateArtistProfile(@Body body: Map<String, String>): Artist

    @Multipart
    @POST("catalog/artists/me/profile-image")
    suspend fun uploadProfileImage(@Part file: MultipartBody.Part): Artist

    @GET("catalog/artists/{artistId}/songs")
    suspend fun getSongsByArtist(@Path("artistId") artistId: String): List<Song>

    @DELETE("catalog/songs/{songId}")
    suspend fun deleteSong(@Path("songId") songId: String)

    @Multipart
    @POST("catalog/songs/{songId}/artwork")
    suspend fun updateSongArtwork(
        @Path("songId") songId: String,
        @Part artwork: MultipartBody.Part
    ): Song

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
        @Part artwork: MultipartBody.Part?,
        @Part("title") title: RequestBody,
        @Part("artistName") artistName: RequestBody,
        @Part("genre") genre: RequestBody,
        @Part("durationMs") durationMs: RequestBody
    ): Song
}
