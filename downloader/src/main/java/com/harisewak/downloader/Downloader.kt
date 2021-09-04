package com.harisewak.downloader

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.harisewak.downloader.room.DownloadsDatabase
import com.harisewak.downloader.workmanager.DownloadWorker
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
object Downloader {

    private lateinit var context: Context
    lateinit var callback: Callback
    lateinit var downloadQueue: List<Request>
    val path by lazy {
        context.filesDir.absolutePath
    }

    var curDownloadPos = -1

    fun with(context: Context, callback: Callback): Downloader {
        this.context = context
        this.callback = callback
        return this
    }

    private val localDatabase: DownloadsDatabase = Room.databaseBuilder(
        this.context,
        DownloadsDatabase::class.java, "downloads-database"
    ).build()

    private val requestDao = localDatabase.requestDao()


    @DelicateCoroutinesApi
    fun enqueue(url: String) {

        GlobalScope.launch(Dispatchers.IO) {

            val request = Request(
                url = url
            )

            requestDao.add(request)

            requestDao.getAll().collectLatest { downloadQueue ->

                if (downloadQueue.size == 1) {

                    curDownloadPos = 0

                    startDownloading()

                }
            }

        }

    }

    private fun startDownloading() {
        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

}