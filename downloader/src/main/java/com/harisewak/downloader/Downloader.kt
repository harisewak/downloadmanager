package com.harisewak.downloader

import android.content.Context
import androidx.work.*
import com.harisewak.downloader.other.REQUEST_ID
import com.harisewak.downloader.room.DatabaseUtil
import com.harisewak.downloader.workmanager.DownloadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/*
* Re-working downloader procedure
* User requests a download
* A request is created in queued state and added to db
* then the request is given to WorkManager
* When executed, request details are updated in db
* */

class Downloader(private val context: Context) {

    val downloadDirPath by lazy {
        context.filesDir.absolutePath
    }

    fun enqueue(url: String) {
        GlobalScope.launch(Dispatchers.IO) {
            download(url)
        }
    }

    // User requests a download
    private suspend fun download(url: String) {

        // A request is created in queued state
        val request = Request(
            url = url
        )

        // and added to db
        val requestId = DatabaseUtil.requestDao(context).add(request)

        // then the request is given to WorkManager
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val inputData = Data.Builder()
            .putLong(REQUEST_ID, requestId)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        // todo find a way to connect download request with work request

        WorkManager.getInstance(context)
            .enqueue(workRequest)

    }

}