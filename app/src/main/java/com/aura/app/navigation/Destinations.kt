package com.aura.app.navigation

/**
 * Central route registry. Every screen in the app has exactly one entry here.
 * Feature modules never hardcode route strings themselves — they reference these.
 */
sealed class Destination(val route: String) {
    data object AuthGate : Destination("auth_gate")
    data object Login : Destination("login")
    data object SignUp : Destination("sign_up")

    data object Home : Destination("home")
    data object Search : Destination("search")
    data object MoodInput : Destination("mood_input")

    data object NowPlaying : Destination("now_playing/{songId}") {
        const val ARG_SONG_ID = "songId"
        fun createRoute(songId: String) = "now_playing/$songId"
    }

    data object Library : Destination("library")
    data object LikedSongs : Destination("liked_songs")
    data object History : Destination("history")
    data object Playlists : Destination("playlists")

    data object PlaylistDetail : Destination("playlist/{playlistId}") {
        const val ARG_PLAYLIST_ID = "playlistId"
        fun createRoute(playlistId: String) = "playlist/$playlistId"
    }

    data object ArtistProfile : Destination("artist/{artistId}") {
        const val ARG_ARTIST_ID = "artistId"
        fun createRoute(artistId: String) = "artist/$artistId"
    }

    data object ArtistUpload : Destination("artist_upload")
    data object Settings : Destination("settings")
}
