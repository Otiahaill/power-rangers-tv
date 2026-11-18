package com.example.prtv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prtv.data.repository.YouTubeRepository
import com.example.prtv.data.repository.YouTubeRepositoryImpl
import com.example.prtv.security.ApiKeyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class ChannelBrowserViewModel @Inject constructor(
    private val repository: YouTubeRepository,
    private val apiKeyManager: ApiKeyManager,
    private val saveApiKey: (String) -> io.reactivex.rxjava3.core.Completable,
    private val saveChannelId: (String) -> io.reactivex.rxjava3.core.Completable,
    private val saveChannelHandle: (String) -> io.reactivex.rxjava3.core.Completable,
    private val apiKeyFlow: io.reactivex.rxjava3.core.Flowable<String>,
    private val channelIdFlow: io.reactivex.rxjava3.core.Flowable<String>
) : ViewModel() {

    private val disposables = CompositeDisposable()

    // UI State
    var handleInput: String = ""
        private set
    var apiKeyInput: String = ""
        private set
    var isLoading: Boolean = false
        private set
    var errorMessage: String? = null
        private set
    var channelResolved: Boolean = false
        private set

    fun setHandleInput(handle: String) {
        handleInput = handle
    }

    fun setApiKeyInput(key: String) {
        apiKeyInput = key
    }

    fun loadChannel(handle: String, apiKey: String) {
        if (handle.isBlank()) {
            errorMessage = "Please enter a channel handle"
            return
        }

        isLoading = true
        errorMessage = null

        // Save API key if provided
        if (apiKey.isNotBlank()) {
            disposables.add(
                saveApiKey(apiKey)
                    .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                    .subscribe(
                        { /* saved */ },
                        { errorMessage = "Failed to save API key: ${it.message}" }
                    )
            )
        }

        // Resolve channel handle
        disposables.add(
            repository.resolveChannelId(handle)
                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .subscribe(
                    { result ->
                        isLoading = false
                        when (result) {
                            is YouTubeRepository.Result.Success -> {
                                channelResolved = true
                                disposables.add(
                                    saveChannelId(result.data)
                                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                                        .subscribe()
                                )
                                disposables.add(
                                    saveChannelHandle(handle)
                                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                                        .subscribe()
                                )
                            }
                            is YouTubeRepository.Result.Error -> {
                                errorMessage = "Failed to resolve channel: ${result.exception.message}"
                            }
                        }
                    },
                    { e -> isLoading = false; errorMessage = "Error: ${e.message}" }
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
