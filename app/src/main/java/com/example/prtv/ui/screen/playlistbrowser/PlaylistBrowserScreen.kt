package com.example.prtv.ui.screen.playlistbrowser

import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.gridItems
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.windowSizeClass
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prtv.R
import com.example.prtv.ui.component.PlaylistCard
import com.example.prtv.ui.theme.PowerRangersTheme
import com.example.prtv.viewmodel.PlaylistBrowserViewModel
import androidx.navigation.compose.NavController

@Composable
fun PlaylistBrowserScreen(
    navController: NavController,
    viewModel: PlaylistBrowserViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val liveStreamAvailable by viewModel.liveStreamAvailable.collectAsStateWithLifecycle()
    val liveStreamVideoId by viewModel.liveStreamVideoId.collectAsStateWithLifecycle()

    // Adaptive grid columns based on window size
    val windowSizeClass = windowSizeClass()
    val columns = when {
        windowSizeClass.windowWidthSizeClass >= androidx.compose.material3.WindowWidthSizeClass.Expanded -> 4 // TV
        windowSizeClass.windowWidthSizeClass >= androidx.compose.material3.WindowWidthSizeClass.Medium -> 2 // Tablet
        else -> 1 // Phone
    }

    // Top App Bar with Live indicator
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(text = "Playlists", color = Color.White, fontSize = 20.sp)
        },
        navigationIcon = {
            IconButton(onClick = { /* back to channel browser */ }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            if (liveStreamAvailable) {
                Box(
                    modifier = Modifier.padding(end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Badge(
                        badgeContent = { Text("LIVE", fontSize = 10.sp, color = Color.White) },
                        backgroundColor = Color.Red
                    ) {
                        IconButton(onClick = {
                            // Navigate to live stream
                        }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.LiveTv,
                                contentDescription = "Live Stream",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFF5200),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFFF5200))
        }
    } else if (playlists.isNotEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(playlists) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    onClick = {
                        // Navigate to video player with playlist
                    }
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.VideoLibrary,
                    contentDescription = "No videos",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(16.dp))
                Text(
                    text = "No playlists found",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
