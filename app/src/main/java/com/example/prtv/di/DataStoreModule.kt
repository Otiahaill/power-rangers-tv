package com.example.prtv.di

import android.content.Context
import androidx.datastore.preferences.core.mutations
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.preferences.rxjava3.RxDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private val API_KEY_KEY = stringPreferencesKey("youtube_api_key")
    private val CHANNEL_ID_KEY = stringPreferencesKey("channel_id")
    private val CHANNEL_HANDLE_KEY = stringPreferencesKey("channel_handle")
    private val THEME_MODE_KEY = intPreferencesKey("theme_mode") // 0=system, 1=light, 2=dark

    @Provides
    @Singleton
    fun provideDataStore(@Suppress("UNUSED_PARAMETER") context: Context): RxDataStore<*> {
        return RxPreferenceDataStoreBuilder(context, "power_rangers_tv_prefs").build()
    }

    @Provides
    @Singleton
    fun provideApiKeyProvider(dataStore: RxDataStore<*>): ApiKeyProvider {
        return object : ApiKeyProvider {
            override fun getApiKey(): String {
                // Synchronous read - in production use Flow/collect
                return ""
            }
        }
    }

    @Provides
    @Singleton
    fun provideApiKeyFlow(dataStore: RxDataStore<*>): io.reactivex.rxjava3.core.Flowable<String> {
        return dataStore.data
            .map { prefs -> prefs[API_KEY_KEY] ?: "" }
            .distinctUntilChanged()
    }

    @Provides
    @Singleton
    fun provideChannelIdFlow(dataStore: RxDataStore<*>): io.reactivex.rxjava3.core.Flowable<String> {
        return dataStore.data
            .map { prefs -> prefs[CHANNEL_ID_KEY] ?: "" }
            .distinctUntilChanged()
    }

    @Provides
    @Singleton
    fun provideThemeModeFlow(dataStore: RxDataStore<*>): io.reactivex.rxjava3.core.Flowable<Int> {
        return dataStore.data
            .map { prefs -> prefs[THEME_MODE_KEY] ?: 0 }
            .distinctUntilChanged()
    }

    @Provides
    @Singleton
    fun provideSaveApiKey(dataStore: RxDataStore<*>): (String) -> Completable {
        return { apiKey ->
            dataStore.updateDataAsync { prefs ->
                prefs.mutations { it[API_KEY_KEY] = apiKey }
            }
        }
    }

    @Provides
    @Singleton
    fun provideSaveChannelId(dataStore: RxDataStore<*>): (String) -> Completable {
        return { channelId ->
            dataStore.updateDataAsync { prefs ->
                prefs.mutations { it[CHANNEL_ID_KEY] = channelId }
            }
        }
    }

    @Provides
    @Singleton
    fun provideSaveChannelHandle(dataStore: RxDataStore<*>): (String) -> Completable {
        return { handle ->
            dataStore.updateDataAsync { prefs ->
                prefs.mutations { it[CHANNEL_HANDLE_KEY] = handle }
            }
        }
    }

    @Provides
    @Singleton
    fun provideSaveThemeMode(dataStore: RxDataStore<*>): (Int) -> Completable {
        return { mode ->
            dataStore.updateDataAsync { prefs ->
                prefs.mutations { it[THEME_MODE_KEY] = mode }
            }
        }
    }
}
