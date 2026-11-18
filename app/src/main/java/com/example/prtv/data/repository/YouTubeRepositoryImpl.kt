package com.example.prtv.data.repository

import com.example.prtv.data.api.YouTubeApiService
import com.example.prtv.data.model.ChannelResponse
import com.example.prtv.data.model.PlaylistListResponse
import com.example.prtv.data.model.SearchResponse
import com.example.prtv.data.model.YouTubePlaylist
import com.example.prtv.data.model.YouTubeVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeRepositoryImpl @Inject constructor(
    private val apiService: YouTubeApiService,
    private val apiKeyProvider: ApiKeyProvider
) : YouTubeRepository {

    override fun getPlaylists(channelId: String) = flow {
        emit(Result.Loading)
        try {
            val apiKey = apiKeyProvider.getApiKey()
            if (apiKey.isBlank()) {
                emit(Result.Error(Exception("API key not configured")))
                return@flow
            }
            val response = apiService.getPlaylists(channelId, apiKey = apiKey).await()
            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!.items))
            } else {
                emit(Result.Error(Exception("Failed to fetch playlists: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPlaylistVideos(playlistId: String) = flow {
        emit(Result.Loading)
        try {
            val apiKey = apiKeyProvider.getApiKey()
            if (apiKey.isBlank()) {
                emit(Result.Error(Exception("API key not configured")))
                return@flow
            }
            val response = apiService.getPlaylistVideos(playlistId, apiKey = apiKey).await()
            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!.items))
            } else {
                emit(Result.Error(Exception("Failed to fetch videos: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getLiveStream(channelId: String) = flow {
        emit(Result.Loading)
        try {
            val apiKey = apiKeyProvider.getApiKey()
            if (apiKey.isBlank()) {
                emit(Result.Error(Exception("API key not configured")))
                return@flow
            }
            val response = apiService.searchVideos(
                channelId = channelId,
                eventType = "live",
                apiKey = apiKey
            ).await()
            if (response.isSuccessful && response.body() != null) {
                val liveVideo = response.body()!!.items.firstOrNull()?.let { item ->
                    item.id.videoId?.let { videoId ->
                        YouTubeVideo(
                            id = videoId,
                            snippet = item.snippet,
                            contentDetails = null
                        )
                    }
                }
                emit(Result.Success(liveVideo))
            } else {
                emit(Result.Error(Exception("Failed to check live stream: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun resolveChannelId(handle: String) = flow {
        emit(Result.Loading)
        try {
            val apiKey = apiKeyProvider.getApiKey()
            if (apiKey.isBlank()) {
                emit(Result.Error(Exception("API key not configured")))
                return@flow
            }
            // Remove @ if present
            val cleanHandle = handle.removePrefix("@")
            val response = apiService.getChannelByHandle(cleanHandle, apiKey = apiKey).await()
            if (response.isSuccessful && response.body() != null) {
                val channelId = response.body()!!.items.firstOrNull()?.id
                if (channelId != null && channelId.isNotBlank()) {
                    emit(Result.Success(channelId))
                } else {
                    emit(Result.Error(Exception("Channel not found for handle: $handle")))
                }
            } else {
                emit(Result.Error(Exception("Failed to resolve channel: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getChannelInfo(channelId: String) = flow {
        emit(Result.Loading)
        try {
            val apiKey = apiKeyProvider.getApiKey()
            if (apiKey.isBlank()) {
                emit(Result.Error(Exception("API key not configured")))
                return@flow
            }
            val response = apiService.getChannelById(channelId, apiKey = apiKey).await()
            if (response.isSuccessful && response.body() != null) {
                val item = response.body()!!.items.firstOrNull()
                if (item != null) {
                    emit(Result.Success(
                        YouTubeRepository.ChannelInfo(
                            channelId = item.id,
                            title = item.snippet.title,
                            customUrl = item.snippet.customUrl
                        )
                    ))
                } else {
                    emit(Result.Error(Exception("Channel not found: $channelId")))
                }
            } else {
                emit(Result.Error(Exception("Failed to get channel info: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)
}

interface ApiKeyProvider {
    fun getApiKey(): String
}
