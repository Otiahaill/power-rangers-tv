package com.example.prtv.ui.screen.videoplayer

import android.content.Context
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prtv.R
import com.example.prtv.ui.theme.PowerRangersTheme
import com.example.prtv.viewmodel.BroadcastScheduler
import com.example.prtv.viewmodel.VideoPlayerViewModel
import androidx.navigation.compose.NavController

@Composable
fun VideoPlayerScreen(
    navController: NavController,
    viewModel: VideoPlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentVideo by viewModel.currentVideo.collectAsStateWithLifecycle()
    val playlistVideos by viewModel.playlistVideos.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val startTimeMs by viewModel.startTimeMs.collectAsStateWithLifecycle()

    var webViewClient by remember { mutableStateOf(YouTubeWebViewClient()) }

    // WebView for YouTube iframe player
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            webViewClient = webViewClient
            webChromeClient = WebChromeClient()
        }
    }

    // Load video when currentVideo changes
    LaunchedEffect(currentVideo?.id, startTimeMs) {
        currentVideo?.let { video ->
            val embedUrl = "https://www.youtube-nocookie.com/embed/${video.id}?autoplay=1&modestbranding=1&controls=1&enablejsapi=1&start=${startTimeMs / 1000}"
            webView.loadUrl(embedUrl)
        }
    }

    // Update start time when seeking
    LaunchedEffect(startTimeMs) {
        if (startTimeMs > 0 && currentVideo != null) {
            // WebView doesn't easily support seeking without reload
            // In a production app, use YouTube IFrame API via JS
        }
    }

    if (currentVideo == null && playlistVideos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No video selected", color = Color.White, fontSize = 18.sp)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    currentVideo?.let { video ->
                        Text(text = video.title, color = Color.White, maxLines = 1, overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Playlist button
                    IconButton(onClick = { /* show playlist */ }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.PlaylistPlay,
                            contentDescription = "Playlist",
                            tint = Color.White
                        )
                    }
                    // Fullscreen button
                    IconButton(onClick = { /* toggle fullscreen */ }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )

            // WebView Player (16:9 aspect ratio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { webView },
                    modifier = Modifier.fillMaxSize(),
                    update = { it.webViewClient = webViewClient }
                )
            }

            // Controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 0.dp,
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color(0xFF1C1C1E)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress indicator (placeholder)
                    androidx.compose.material3.LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFF5200),
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )

                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(16.dp))

                    // Playback controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { viewModel.playPrevious() }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { /* play/pause */ }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.playNext() }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White
                            )
                        }
                    }

                    // Up Next card (if playlist has more videos)
                    playlistVideos.drop(currentIndex + 1).firstOrNull()?.let { nextVideo ->
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(16.dp))
                        Text(
                            text = "Up Next: ${nextVideo.title}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// Simple WebViewClient for YouTube
class YouTubeWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        // Handle YouTube links in-app
        return url?.contains("youtube.com") == true || url?.contains("youtu.be") == true
    }
}
