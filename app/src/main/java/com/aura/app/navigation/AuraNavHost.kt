package com.aura.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.aura.feature.artist.ArtistProfileScreen
import com.aura.feature.artist.ArtistUploadScreen
import com.aura.feature.home.HomeScreen
import com.aura.feature.library.LibraryScreen
import com.aura.feature.moodinput.MoodInputScreen
import com.aura.feature.nowplaying.NowPlayingScreen
import com.aura.feature.playlist.PlaylistDetailScreen
import com.aura.feature.search.SearchScreen
import com.aura.feature.settings.SettingsScreen

/**
 * Top-level NavHost wiring every feature graph together.
 * Immersive visuals (Rive + Canvas layers) only ever render inside NowPlayingScreen —
 * every other destination stays on the neutral Aura theme.
 */
@Composable
fun AuraNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route
    ) {
        composable(Destination.Home.route) {
            HomeScreen(
                onSongClick = { songId ->
                    navController.navigate(Destination.NowPlaying.createRoute(songId))
                },
                onSearchClick = { navController.navigate(Destination.Search.route) },
                onMoodClick = { navController.navigate(Destination.MoodInput.route) }
            )
        }

        composable(Destination.Search.route) {
            SearchScreen(
                onSongClick = { songId ->
                    navController.navigate(Destination.NowPlaying.createRoute(songId))
                }
            )
        }

        composable(Destination.MoodInput.route) {
            MoodInputScreen(
                onSongSelected = { songId ->
                    navController.navigate(Destination.NowPlaying.createRoute(songId))
                }
            )
        }

        composable(
            route = Destination.NowPlaying.route,
            arguments = listOf(navArgument(Destination.NowPlaying.ARG_SONG_ID) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val songId = backStackEntry.arguments?.getString(Destination.NowPlaying.ARG_SONG_ID).orEmpty()
            NowPlayingScreen(songId = songId)
        }

        composable(Destination.Library.route) {
            LibraryScreen(
                onPlaylistClick = { playlistId ->
                    navController.navigate(Destination.PlaylistDetail.createRoute(playlistId))
                }
            )
        }

        composable(
            route = Destination.PlaylistDetail.route,
            arguments = listOf(navArgument(Destination.PlaylistDetail.ARG_PLAYLIST_ID) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString(Destination.PlaylistDetail.ARG_PLAYLIST_ID).orEmpty()
            PlaylistDetailScreen(playlistId = playlistId)
        }

        composable(
            route = Destination.ArtistProfile.route,
            arguments = listOf(navArgument(Destination.ArtistProfile.ARG_ARTIST_ID) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString(Destination.ArtistProfile.ARG_ARTIST_ID).orEmpty()
            ArtistProfileScreen(
                artistId = artistId,
                onUploadClick = { navController.navigate(Destination.ArtistUpload.route) }
            )
        }

        composable(Destination.ArtistUpload.route) {
            ArtistUploadScreen()
        }

        composable(Destination.Settings.route) {
            SettingsScreen()
        }
    }
}
