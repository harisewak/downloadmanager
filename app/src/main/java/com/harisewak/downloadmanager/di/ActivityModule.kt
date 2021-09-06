package com.harisewak.downloadmanager.di

import com.harisewak.ui.DownloadListAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@InstallIn(ActivityRetainedComponent::class)
@Module
class ActivityModule {

    @Provides
    @ActivityRetainedScoped
    fun provideDownloadListAdapter() = DownloadListAdapter()

}