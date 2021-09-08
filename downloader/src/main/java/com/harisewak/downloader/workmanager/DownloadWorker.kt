package com.harisewak.downloader.workmanager

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.harisewak.downloader.DownloadStatus
import com.harisewak.downloader.other.*
import com.harisewak.downloader.room.DatabaseUtil
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val requestDao by lazy {
        DatabaseUtil
            .requestDao(appContext)
    }

    private val downloadDirPath = appContext.filesDir.absolutePath


    override suspend fun doWork(): Result {
        return executeDownloadRequest()
    }

//    When executed, request details are updated in db
    suspend fun executeDownloadRequest(): Result {

        val requestId = inputData.getLong(REQUEST_ID, -1L)

        if (requestId == -1L) {

            val inputData = Data.Builder()
                .putInt(REASON, REASON_DATABASE_ERROR)
                .putString(MESSAGE, "request ID cannot be negative")
                .build()

            return Result.failure(inputData)
        }

        val request = requestDao.findById(requestId)

        request.isDownloading = true

        val downloadDir = File(downloadDirPath)

        // first exit
        if (!downloadDir.exists()) {
            request.status = DownloadStatus.FAILED
            request.isDownloading = false
            requestDao.update(request)

            val inputData = Data.Builder()
                .putInt(REASON, REASON_STORAGE_ERROR)
                .putString(MESSAGE, "Download directory does not exist!")
                .build()

            return Result.failure(inputData)
        }

        val fileName = Uri.parse(request.url).lastPathSegment

        val filePath = downloadDirPath + File.separator + fileName

        val file = File(filePath)

        var urlConnection: HttpURLConnection? = null
        val inputStream: InputStream?
        var readCounter = 0

        try {

            val url = URL(request.url)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.setRequestProperty("Connection", "Keep-Alive")

            val responseCode = urlConnection.responseCode

            // second exit
            if (responseCode != HttpURLConnection.HTTP_OK) {

                request.status = DownloadStatus.FAILED
                request.isDownloading = false

                requestDao.update(request)

                val inputData = Data.Builder()
                    .putInt(REASON, REASON_RESPONSE_NOT_OK)
                    .build()

                return Result.failure(inputData)
            }

            val contentLength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                urlConnection.contentLengthLong
            } else {
                urlConnection.contentLength.toLong()
            }

            request.total = contentLength

            inputStream = BufferedInputStream(urlConnection.inputStream)

            val fileOutputStream = FileOutputStream(file)

            var bytesRead: Int

            val buffer = ByteArray(BUFFER_SIZE)

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {

                readCounter++

                fileOutputStream.write(buffer, 0, bytesRead)

                request.downloaded += bytesRead.toLong()

                if (readCounter % 64 == 0) {

                    val updateRequest = requestDao.findById(requestId)

                    // check if request was cancelled, cancel download and delete request from database
                    if (updateRequest.status == DownloadStatus.CANCELLED) {

                        updateRequest.isDownloading = false

                        requestDao.delete(updateRequest)

                        val inputData = Data.Builder()
                            .putInt(REASON, REASON_CANCELLED)
                            .build()

                        return Result.failure(inputData)
                    }

                    // updating progress
                    if (readCounter % 16 == 0) {
                        requestDao.update(request)
                    }

                }
            }

            request.isDownloading = false
            request.status = DownloadStatus.DOWNLOADED

            inputStream.close()

            // update request details in db
            requestDao.update(request)

            return Result.success()

        } catch (e: IOException) {

            e.printStackTrace()

            request.isDownloading = false
            request.status = DownloadStatus.FAILED

            requestDao.update(request)

            val inputData = Data.Builder()
                .putInt(REASON, REASON_NETWORK_FAILURE)
                .putString(MESSAGE, e.message)
                .build()

            return Result.retry()

        } finally {

            urlConnection?.disconnect()

        }

    }

}