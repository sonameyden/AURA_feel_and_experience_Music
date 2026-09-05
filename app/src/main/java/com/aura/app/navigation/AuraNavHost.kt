package com.aura.app.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
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
import com.aura.feature.library.*
import com.aura.feature.moodinput.MoodInputScreen
import com.aura.feature.nowplaying.NowPlayingScreen
import com.aura.feature.playlist.PlaylistDetailScreen
import com.aura.feature.search.SearchScreen
import com.aura.feature.settings.SettingsScreen

@Composable
fun AuraNavHost(
    navController: NavHostController,
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val mainRoutes = setOf(
        Destination.Home.route,
        Destination.Search.route,
        Destination.Library.route,
        Destination.Settings.route
    )

    val showBottomBar = currentRoute in mainRoutes
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

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

    val showMiniPlayer = song != null && currentRoute !in hideMiniPlayerRoutes

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                if (showMiniPlayer) {
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
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 0.dp) // Anchored tight
                    )
                }

                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 0.dp) // Anchored to bottom padding
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .shadow(
                                    elevation = if (isLight) 6.dp else 0.dp,
                                    shape = RoundedCornerShape(32.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.08f),
                                    spotColor = Color.Black.copy(alpha = 0.08f)
                                ),
                            shape = RoundedCornerShape(32.dp),
                            color = if (isLight) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets(0, 0, 0, 0)
                            ) {
                                val navItemColors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (isLight) Color(0xFF403746) else MaterialTheme.colorScheme.onSurface,
                                    selectedTextColor = if (isLight) Color(0xFF403746) else MaterialTheme.colorScheme.onSurface,
                                    unselectedIconColor = if (isLight) Color(0xFF625D66) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    unselectedTextColor = if (isLight) Color(0xFF69636D) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    indicatorColor = if (isLight) Color(0xFFE9DDF5) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )

                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                                    selected = currentRoute == Destination.Home.route,
                                    colors = navItemColors,
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
                                    label = { Text("Search", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                                    selected = currentRoute == Destination.Search.route,
                                    colors = navItemColors,
                                    onClick = {
                                        if (currentRoute != Destination.Search.route) {
                                            navController.navigate(Destination.Search.route)
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                                    label = { Text("Library", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                                    selected = currentRoute == Destination.Library.route,
                                    colors = navItemColors,
                                    onClick = {
                                        if (currentRoute != Destination.Library.route) {
                                            navController.navigate(Destination.Library.route)
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                                    selected = currentRoute == Destination.Settings.route,
                                    colors = navItemColors,
                                    onClick = {
                                        if (currentRoute != Destination.Settings.route) {
                                            navController.navigate(Destination.Settings.route)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                        onArtistClick = { artistId ->
                            navController.navigate(Destination.ArtistProfile.createRoute(artistId))
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
                            onArtistClick = { artistId ->
                                navController.navigate(Destination.ArtistProfile.createRoute(artistId))
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
                        onBackClick = { navController.popBackStack() },
                        onArtistClick = { artistId ->
                            navController.navigate(Destination.ArtistProfile.createRoute(artistId))
                        }
                    )
                }

                composable(Destination.Library.route) {
                    AuraBackground {
                        LibraryScreen(
                            onPlaylistClick = { playlistId ->
                                navController.navigate(Destination.PlaylistDetail.createRoute(playlistId))
                            },
                            onArtistClick = { artistId ->
                                navController.navigate(Destination.ArtistProfile.createRoute(artistId))
                            },
                            onLikedSongsClick = { navController.navigate(Destination.LikedSongs.route) },
                            onHistoryClick = { navController.navigate(Destination.History.route) },
                            onPlaylistsClick = { navController.navigate(Destination.Playlists.route) },
                            onSongClick = { songId ->
                                navController.navigate(Destination.NowPlaying.createRoute(songId)) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                composable(Destination.LikedSongs.route) {
                    AuraBackground {
                        LikedSongsScreen(
                            onBackClick = { navController.popBackStack() },
                            onSongClick = { songId ->
                                navController.navigate(Destination.NowPlaying.createRoute(songId)) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                composable(Destination.History.route) {
                    AuraBackground {
                        HistoryScreen(
                            onBackClick = { navController.popBackStack() },
                            onSongClick = { songId ->
                                navController.navigate(Destination.NowPlaying.createRoute(songId)) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                composable(Destination.Playlists.route) {
                    AuraBackground {
                        PlaylistsScreen(
                            onBackClick = { navController.popBackStack() },
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
                        PlaylistDetailScreen(
                            playlistId = playlistId,
                            onBackClick = { navController.popBackStack() },
                            onSongClick = { songId ->
                                navController.navigate(Destination.NowPlaying.createRoute(songId)) {
                                    launchSingleTop = true
                                }
                            }
                        )
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
                            },
                            isDarkTheme = darkTheme,
                            onThemeChange = onThemeChange
                        )
                    }
                }
            }
        }
    }
}
