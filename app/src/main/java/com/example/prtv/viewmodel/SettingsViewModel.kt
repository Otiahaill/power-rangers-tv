package com.example.prtv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prtv.di.ApiKeyProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyProvider: ApiKeyProvider,
    private val saveApiKey: (String) -> io.reactivex.rxjava3.core.Completable,
    private val saveThemeMode: (Int) -> Completable,
    private val apiKeyFlow: Flowable<String>,
    private val themeModeFlow: Flowable<Int>
) : ViewModel() {

    private val disposables = CompositeDisposable()

    // UI State
    var currentApiKey: String = ""
        private set
    var themeMode: Int = 0 // 0=system, 1=light, 2=dark
        private set

    init {
        observeFlows()
    }

    private fun observeFlows() {
        disposables.add(
            apiKeyFlow
                .subscribeOn(Schedulers.io())
                .subscribe { currentApiKey = it }
        )

        disposables.add(
            themeModeFlow
                .subscribeOn(Schedulers.io())
                .subscribe { themeMode = it }
        )
    }

    fun updateApiKey(newKey: String) {
        disposables.add(
            saveApiKey(newKey)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { currentApiKey = newKey },
                    { /* handle error */ }
                )
        )
    }

    fun updateThemeMode(newMode: Int) {
        if (newMode !in 0..2) return
        disposables.add(
            saveThemeMode(newMode)
                .subscribeOn(Schedulers.io())
                .subscribe { themeMode = newMode }
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
