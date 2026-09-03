package com.aura.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.aura.core.auth.AuthState
import com.aura.feature.artist.ArtistProfileScreen
import com.aura.feature.artist.ArtistUploadScreen
import com.aura.feature.auth.LoginScreen
import com.aura.feature.auth.SignUpScreen
import com.aura.feature.home.HomeScreen
import com.aura.feature.library.LibraryScreen
import com.aura.feature.moodinput.MoodInputScreen
import com.aura.feature.nowplaying.NowPlayingScreen
import com.aura.feature.playlist.PlaylistDetailScreen
import com.aura.feature.search.SearchScreen
import com.aura.feature.settings.SettingsScreen

/**
 * Top-level NavHost. Starts at AuthGate, which observes AuthRepository.authState
 * (via NavAuthViewModel below) and routes to either Login or Home, popping
 * itself off the back stack either way so the user can never navigate "back"
 * into the gate. Immersive visuals (Rive + Canvas layers) only ever render
 * inside NowPlayingScreen — every other destination stays on the neutral
 * Aura theme.
 */
@Composable
fun AuraNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destination.AuthGate.route
    ) {
        composable(Destination.AuthGate.route) {
            val authViewModel: NavAuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()

            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Authenticated -> navController.navigate(Destination.Home.route) {
                        popUpTo(Destination.AuthGate.route) { inclusive = true }
                    }
                    AuthState.Unauthenticated -> navController.navigate(Destination.Login.route) {
                        popUpTo(Destination.AuthGate.route) { inclusive = true }
                    }
                    AuthState.Loading -> Unit // stay on the gate until Firebase reports a real state
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        composable(Destination.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Destination.Home.route) {
                        popUpTo(Destination.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(Destination.SignUp.route) }
            )
        }

        composable(Destination.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Destination.Home.route) {
                        popUpTo(Destination.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Destination.Home.route) {
            HomeScreen(
                onSongClick = { songId ->
                    navController.navigate(Destination.NowPlaying.createRoute(songId))
                },
                onSearchClick = { navController.navigate(Destination.Search.route) },
                onMoodClick = { navController.navigate(Destination.MoodInput.route) },
                onProfileClick = { navController.navigate(Destination.Settings.route) }
            )
        }

        composable(Destination.Search.route) {
            SearchScreen(
                onSongClick = { songId ->
                    navController.navigate(Destination.NowPlaying.createRoute(songId))
                },
                onBackClick = { navController.popBackStack() }
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
            NowPlayingScreen(
                songId = songId,
                onBackClick = { navController.popBackStack() }
            )
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
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Destination.Login.route) {
                        popUpTo(0) { inclusive = true } // clear entire back stack — no navigating "back" into a logged-out session
                    }
                }
            )
        }
    }
}
