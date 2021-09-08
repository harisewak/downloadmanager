package com.harisewak.downloader

import android.annotation.SuppressLint
import androidx.lifecycle.LifecycleService
import androidx.work.impl.foreground.SystemForegroundService

@SuppressLint("RestrictedApi")
class DownloadService: SystemForegroundService()