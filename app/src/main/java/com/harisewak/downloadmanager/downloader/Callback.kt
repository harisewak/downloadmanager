package com.harisewak.downloadmanager.downloader

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import java.io.File

// Callback events about an initiated download
interface Callback {

    fun progress(progress: Long, total: Long)

    fun success()

    fun failure(message: String)
}