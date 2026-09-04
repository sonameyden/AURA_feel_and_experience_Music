package com.aura.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.aura.app.player.MiniPlayerViewModel
import com.aura.core.audio.PlaybackState
import com.aura.core.auth.AuthState
import com.aura.core.designsystem.components.AuraBackground
import com.aura.core.designsystem.components.MiniPlayerBar
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

@Composable
fun AuraNavHost(navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val mainRoutes = setOf(
        Destination.Home.route,
        Destination.Search.route,
        Destination.Library.route,
        Destination.Settings.route
    )

    val showBottomBar = currentRoute in mainRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == Destination.Home.route,
                        onClick = {
                            if (currentRoute != Destination.Home.route) {
                                navController.navigate(Destination.Home.route) {
                                    popUpTo(Destination.Home.route) { inclusive = true }
                                }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        selected = currentRoute == Destination.Search.route,
                        onClick = {
                            if (currentRoute != Destination.Search.route) {
                                navController.navigate(Destination.Search.route)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                        label = { Text("Library") },
                        selected = currentRoute == Destination.Library.route,
                        onClick = {
                            if (currentRoute != Destination.Library.route) {
                                navController.navigate(Destination.Library.route)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == Destination.Settings.route,
                        onClick = {
                            if (currentRoute != Destination.Settings.route) {
                                navController.navigate(Destination.Settings.route)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                            AuthState.Loading -> Unit
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
                            navController.navigate(Destination.NowPlaying.createRoute(songId)) {
                                launchSingleTop = true
                            }
                        },
                        onMoodClick = { navController.navigate(Destination.MoodInput.route) },
                        onProfileClick = { navController.navigate(Destination.Settings.route) }
                    )
                }

                    composable(Destination.Search.route) {
                        AuraBackground {
                            SearchScreen(
                                onSongClick = { songId ->
                                    navController.navigate(Destination.NowPlaying.createRoute(songId)) {
                                        launchSingleTop = true
                                    }
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                }

                composable(Destination.MoodInput.route) {
                    MoodInputScreen(
                        onSongSelected = { songId ->
                            navController.navigate(Destination.NowPlaying.createRoute(songId)) {
                                launchSingleTop = true
                                popUpTo(Destination.MoodInput.route) { inclusive = true }
                            }
                        },
                        onBackClick = { navController.popBackStack() }
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
                    AuraBackground {
                        LibraryScreen(
                            onPlaylistClick = { playlistId ->
                                navController.navigate(Destination.PlaylistDetail.createRoute(playlistId))
                            }
                        )
                    }
                }

                composable(
                    route = Destination.PlaylistDetail.route,
                    arguments = listOf(navArgument(Destination.PlaylistDetail.ARG_PLAYLIST_ID) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getString(Destination.PlaylistDetail.ARG_PLAYLIST_ID).orEmpty()
                    AuraBackground {
                        PlaylistDetailScreen(playlistId = playlistId)
                    }
                }

                composable(
                    route = Destination.ArtistProfile.route,
                    arguments = listOf(navArgument(Destination.ArtistProfile.ARG_ARTIST_ID) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val artistId = backStackEntry.arguments?.getString(Destination.ArtistProfile.ARG_ARTIST_ID).orEmpty()
                    AuraBackground {
                        ArtistProfileScreen(
                            artistId = artistId,
                            onBackClick = { navController.popBackStack() },
                            onUploadClick = { navController.navigate(Destination.ArtistUpload.route) }
                        )
                    }
                }

                composable(Destination.ArtistUpload.route) {
                    AuraBackground {
                        ArtistUploadScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Destination.Settings.route) {
                    AuraBackground {
                        SettingsScreen(
                            onBackClick = { navController.popBackStack() },
                            onLoggedOut = {
                                navController.navigate(Destination.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onArtistProfileClick = { artistId ->
                                navController.navigate(Destination.ArtistProfile.createRoute(artistId))
                            }
                        )
                    }
                }
            }

            val miniPlayerViewModel: MiniPlayerViewModel = hiltViewModel()
            val playbackState by miniPlayerViewModel.playbackState.collectAsState()

            val hideMiniPlayerRoutes = setOf(
                Destination.AuthGate.route,
                Destination.Login.route,
                Destination.SignUp.route,
                Destination.NowPlaying.route
            )

            val song = when (val state = playbackState) {
                is PlaybackState.Playing -> state.song
                is PlaybackState.Paused -> state.song
                is PlaybackState.Buffering -> state.song
                else -> null
            }

            if (song != null && currentRoute !in hideMiniPlayerRoutes) {
                MiniPlayerBar(
                    songTitle = song.title,
                    artistName = song.artistName,
                    artworkUrl = song.artworkUrl,
                    isPlaying = playbackState is PlaybackState.Playing || playbackState is PlaybackState.Buffering,
                    onPlayPauseClick = miniPlayerViewModel::onPlayPauseClick,
                    onBarClick = { 
                        navController.navigate(Destination.NowPlaying.createRoute(song.id)) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                )
            }
        }
    }
}
