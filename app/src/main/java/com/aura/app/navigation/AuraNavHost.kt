package com.aura.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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

    val authViewModel: NavAuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val currentUserId = (authState as? AuthState.Authenticated)?.userId

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

    fun navigateToNowPlaying(songId: String) {
        navController.navigate(Destination.NowPlaying.createRoute(songId)) {
            popUpTo(Destination.NowPlaying.route) { inclusive = true }
            launchSingleTop = true
        }
    }

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
                        onBarClick = { navigateToNowPlaying(song.id) },
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 0.dp)
                    )
                }

                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 0.dp)
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
            // PERSISTENT BACKGROUND: Ensures non-immersive screens share a static atmosphere
            AuraBackground {
                NavHost(
                    navController = navController,
                    startDestination = Destination.AuthGate.route,
                    enterTransition = { fadeIn(animationSpec = tween(400)) },
                    exitTransition = { fadeOut(animationSpec = tween(400)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(400)) },
                    popExitTransition = { fadeOut(animationSpec = tween(400)) }
                ) {
                    composable(Destination.AuthGate.route) {
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

                    composable(
                        route = Destination.Home.route,
                        exitTransition = { fadeOut(animationSpec = tween(300)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(300)) }
                    ) {
                        HomeScreen(
                            onSongClick = { songId -> navigateToNowPlaying(songId) },
                            onArtistClick = { artistId ->
                                navController.navigate(Destination.ArtistProfile.createRoute(artistId))
                            },
                            onMoodClick = { navController.navigate(Destination.MoodInput.route) },
                            onProfileClick = { navController.navigate(Destination.Settings.route) }
                        )
                    }

                    composable(Destination.Search.route) {
                        SearchScreen(
                            onSongClick = { songId -> navigateToNowPlaying(songId) },
                            onArtistClick = { artistId ->
                                navController.navigate(Destination.ArtistProfile.createRoute(artistId))
                            },
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Destination.MoodInput.route,
                        enterTransition = {
                            fadeIn(animationSpec = tween(500)) + slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = tween(500)
                            )
                        },
                        exitTransition = { fadeOut(animationSpec = tween(400)) },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(400)) + slideOutVertically(
                                targetOffsetY = { it / 4 },
                                animationSpec = tween(400)
                            )
                        }
                    ) {
                        MoodInputScreen(
                            onSongSelected = { songId ->
                                navController.navigate(Destination.NowPlaying.createRoute(songId)) {
                                    launchSingleTop = true
                                    popUpTo(Destination.Home.route)
                                }
                            },
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Destination.NowPlaying.route,
                        arguments = listOf(navArgument(Destination.NowPlaying.ARG_SONG_ID) {
                            type = NavType.StringType
                        }),
                        enterTransition = {
                            fadeIn(animationSpec = tween(600)) + slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(600)
                            )
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(500)) + slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(500)
                            )
                        }
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
                            onSongClick = { songId -> navigateToNowPlaying(songId) }
                        )
                    }

                    composable(Destination.LikedSongs.route) {
                        LikedSongsScreen(
                            onBackClick = { navController.popBackStack() },
                            onSongClick = { songId -> navigateToNowPlaying(songId) }
                        )
                    }

                    composable(Destination.History.route) {
                        HistoryScreen(
                            onBackClick = { navController.popBackStack() },
                            onSongClick = { songId -> navigateToNowPlaying(songId) }
                        )
                    }

                    composable(Destination.Playlists.route) {
                        PlaylistsScreen(
                            onBackClick = { navController.popBackStack() },
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
                        PlaylistDetailScreen(
                            playlistId = playlistId,
                            onBackClick = { navController.popBackStack() },
                            onSongClick = { songId -> navigateToNowPlaying(songId) }
                        )
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
                            onBackClick = { navController.popBackStack() },
                            onUploadClick = { navController.navigate(Destination.ArtistUpload.route) },
                            onSongClick = { songId -> navigateToNowPlaying(songId) },
                            currentUserId = currentUserId
                        )
                    }

                    composable(Destination.ArtistUpload.route) {
                        ArtistUploadScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(Destination.Settings.route) {
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
