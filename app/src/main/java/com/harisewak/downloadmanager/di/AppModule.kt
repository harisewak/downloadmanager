package com.harisewak.downloadmanager.di

import android.content.Context
import com.harisewak.downloader.Downloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideDownloader(@ApplicationContext context: Context) = Downloader(context)

}