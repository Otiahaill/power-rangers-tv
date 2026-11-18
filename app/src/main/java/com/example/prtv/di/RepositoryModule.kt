package com.example.prtv.di

import com.example.prtv.data.repository.YouTubeRepository
import com.example.prtv.data.repository.YouTubeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideYouTubeRepository(impl: YouTubeRepositoryImpl): YouTubeRepository {
        return impl
    }
}
