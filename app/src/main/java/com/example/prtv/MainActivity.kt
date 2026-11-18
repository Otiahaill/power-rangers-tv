package com.example.prtv

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPoint
import dagger.hilt.android.HiltAndroidApp
import com.example.prtv.di.ApiKeyProvider
import com.example.prtv.ui.screen.channelbrowser.ChannelBrowserScreen
import com.example.prtv.ui.screen.playlistbrowser.PlaylistBrowserScreen
import com.example.prtv.ui.screen.videoplayer.VideoPlayerScreen
import com.example.prtv.ui.screen.settings.SettingsScreen
import com.example.prtv.ui.theme.PowerRangersTheme
import com.example.prtv.viewmodel.BroadcastScheduler
import com.example.prtv.viewmodel.ChannelBrowserViewModel
import com.example.prtv.viewmodel.PlaylistBrowserViewModel
import com.example.prtv.viewmodel.SettingsViewModel
import com.example.prtv.viewmodel.VideoPlayerViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PowerRangersTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppNavHost()
                }
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val startDestination = "channel_browser"

    NavHost(navController, startDestination = startDestination) {
        composable(route = "channel_browser") {
            ChannelBrowserScreen(navController = navController)
        }

        composable(route = "playlist_browser") {
            PlaylistBrowserScreen(navController = navController)
        }

        composable(
            route = "video_player",
            arguments = listOf(
                androidx.navigation.NavArgument("videoId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.NavArgument("playlistId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.NavArgument("index") { type = androidx.navigation.NavType.IntType },
                androidx.navigation.NavArgument("startMs") { type = androidx.navigation.NavType.LongType }
            )
        ) {
            VideoPlayerScreen(navController = navController)
        }

        composable(route = "settings") {
            SettingsScreen(navController = navController)
        }
    }
}

// Helper functions for navigation
fun NavController.navigateToPlaylistBrowser() {
    navigate("playlist_browser") {
        popUpTo(findStartDestination().id) { saveState = true }
        launchSingleTop = true
    }
}

fun NavController.navigateToVideoPlayer(
    videoId: String,
    playlistId: String? = null,
    index: Int = 0,
    startMs: Long = 0
) {
    val args = buildString {
        append("video_player?videoId=$videoId")
        playlistId?.let { append("&playlistId=$it") }
        append("&index=0")
        append("&startMs=0")
    }
    navigate(args)
}

fun NavController.navigateToSettings() {
    navigate("settings")
}

fun NavController.navigateToChannelBrowser() {
    navigate("channel_browser") {
        popUpTo(findStartDestination().id) { saveState = true }
        launchSingleTop = true
    }
}
