package com.example.prtv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prtv.data.model.YouTubePlaylist
import com.example.prtv.data.repository.YouTubeRepository
import com.example.prtv.di.ApiKeyProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class PlaylistBrowserViewModel @Inject constructor(
    private val repository: YouTubeRepository,
    private val apiKeyProvider: ApiKeyProvider,
    private val channelIdFlow: Flowable<String>
) : ViewModel() {

    private val disposables = CompositeDisposable()

    // UI State
    var playlists: List<YouTubePlaylist> = emptyList()
        private set
    var isLoading: Boolean = false
        private set
    var errorMessage: String? = null
        private set
    var liveStreamAvailable: Boolean = false
        private set
    var liveStreamVideoId: String? = null
        private set

    fun loadPlaylists(channelId: String) {
        if (channelId.isBlank()) return

        isLoading = true
        errorMessage = null

        disposables.add(
            repository.getPlaylists(channelId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { result ->
                        isLoading = false
                        when (result) {
                            is YouTubeRepository.Result.Success -> {
                                playlists = result.data
                            }
                            is YouTubeRepository.Result.Error -> {
                                errorMessage = "Failed to load playlists: ${result.exception.message}"
                            }
                        }
                    },
                    { e -> isLoading = false; errorMessage = "Error: ${e.message}" }
                )
        )

        // Also check for live stream
        checkLiveStream(channelId)
    }

    private fun checkLiveStream(channelId: String) {
        disposables.add(
            repository.getLiveStream(channelId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { result ->
                        when (result) {
                            is YouTubeRepository.Result.Success -> {
                                liveStreamAvailable = result.data != null
                                liveStreamVideoId = result.data?.id
                            }
                            is YouTubeRepository.Result.Error -> {
                                // Ignore live stream errors
                            }
                        }
                    }
                )
        )
    }

    fun clearError() {
        errorMessage = null
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
