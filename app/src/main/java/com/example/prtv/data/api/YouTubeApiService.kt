package com.example.prtv.data.api

import com.example.prtv.data.model.ChannelResponse
import com.example.prtv.data.model.PlaylistListResponse
import com.example.prtv.data.model.SearchResponse
import com.example.prtv.data.model.VideoListResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface YouTubeApiService {

    @GET("playlists")
    fun getPlaylists(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("channelId") channelId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("key") apiKey: String,
        @Query("pageToken") pageToken: String? = null
    ): Call<PlaylistListResponse>

    @GET("playlistItems")
    fun getPlaylistVideos(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("key") apiKey: String,
        @Query("pageToken") pageToken: String? = null
    ): Call<VideoListResponse>

    @GET("search")
    fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("channelId") channelId: String,
        @Query("type") type: String = "video",
        @Query("eventType") eventType: String? = null,
        @Query("maxResults") maxResults: Int = 50,
        @Query("key") apiKey: String
    ): Call<SearchResponse>

    @GET("channels")
    fun getChannelByHandle(
        @Query("part") part: String = "snippet",
        @Query("forHandle") handle: String,
        @Query("key") apiKey: String
    ): Call<ChannelResponse>

    @GET("channels")
    fun getChannelById(
        @Query("part") part: String = "snippet",
        @Query("id") id: String,
        @Query("key") apiKey: String
    ): Call<ChannelResponse>

    @GET("videos")
    fun getVideos(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("id") videoIds: String,
        @Query("key") apiKey: String
    ): Call<VideoListResponse>
}
