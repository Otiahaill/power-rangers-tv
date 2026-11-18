package com.example.prtv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prtv.data.model.YouTubeVideo
import com.example.prtv.data.repository.YouTubeRepository
import com.example.prtv.di.ApiKeyProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val repository: YouTubeRepository,
    private val apiKeyProvider: ApiKeyProvider
) : ViewModel() {

    private val disposables = CompositeDisposable()

    // UI State
    var currentVideo: YouTubeVideo? = null
        private set
    var playlistVideos: List<com.example.prtv.data.model.YouTubeVideo> = emptyList()
        private set
    var currentIndex: Int = 0
        private set
    var isLoading: Boolean = false
        private set
    var errorMessage: String? = null
        private set
    var startTimeMs: Long = 0
        private set

    fun loadVideo(video: YouTubeVideo, playlistId: String? = null, startMs: Long = 0) {
        currentVideo = video
        startTimeMs = startMs
        errorMessage = null
    }

    fun loadPlaylistVideos(playlistId: String) {
        isLoading = true
        errorMessage = null

        disposables.add(
            repository.getPlaylistVideos(playlistId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { result ->
                        isLoading = false
                        when (result) {
                            is com.example.prtv.data.repository.YouTubeRepository.Result.Success -> {
                                playlistVideos = result.data
                                if (playlistVideos.isNotEmpty() && currentVideo == null) {
                                    currentVideo = playlistVideos[0]
                                    currentIndex = 0
                                }
                            }
                            is com.example.prtv.data.repository.YouTubeRepository.Result.Error -> {
                                errorMessage = "Failed to load videos: ${result.exception.message}"
                            }
                        }
                    },
                    { e -> isLoading = false; errorMessage = "Error: ${e.message}" }
                )
        )
    }

    fun playNext() {
        if (playlistVideos.isNotEmpty() && currentIndex < playlistVideos.size - 1) {
            currentIndex++
            currentVideo = playlistVideos[currentIndex]
            startTimeMs = 0
        }
    }

    fun playPrevious() {
        if (playlistVideos.isNotEmpty() && currentIndex > 0) {
            currentIndex--
            currentVideo = playlistVideos[currentIndex]
            startTimeMs = 0
        }
    }

    fun playAt(index: Int) {
        if (playlistVideos.indices.contains(index)) {
            currentIndex = index
            currentVideo = playlistVideos[index]
            startTimeMs = 0
        }
    }

    fun updateStartTime(ms: Long) {
        startTimeMs = ms
    }

    fun clearError() {
        errorMessage = null
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
