package com.example.prtv.data.repository

import com.example.prtv.data.model.YouTubePlaylist
import com.example.prtv.data.model.YouTubeVideo
import kotlinx.coroutines.flow.Flow

interface YouTubeRepository {

    /**
     * Get all playlists for a channel
     */
    fun getPlaylists(channelId: String): Flow<Result<List<YouTubePlaylist>>>

    /**
     * Get videos for a playlist
     */
    fun getPlaylistVideos(playlistId: String): Flow<Result<List<YouTubeVideo>>>

    /**
     * Search for live streams on a channel
     */
    fun getLiveStream(channelId: String): Flow<Result<YouTubeVideo?>>

    /**
     * Resolve a channel handle (e.g., @PowerRangers) to channel ID
     */
    fun resolveChannelId(handle: String): Flow<Result<String>>

    /**
     * Get channel info by ID
     */
    fun getChannelInfo(channelId: String): Flow<Result<ChannelInfo>>

    data class ChannelInfo(
        val channelId: String,
        val title: String,
        val customUrl: String?
    )
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
