package com.harisewak.downloader.workmanager

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.harisewak.downloader.DownloadStatus
import com.harisewak.downloader.Downloader
import com.harisewak.downloader.other.logd
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

const val BUFFER_SIZE = 4096


class DownloadWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        performSequentialDownloads()

        return Result.success()
    }

    fun performSequentialDownloads() {

        // exit if all requests have been consumed
        if (Downloader.curDownloadPos >= Downloader.downloadQueue.size) {
            return
        }

        val request = Downloader.downloadQueue[Downloader.curDownloadPos]

        logd("processing request: ${request.url}")

        request.isDownloading = true

        val path = File(Downloader.path)

        if (!path.exists()) {
            Downloader.callback.failure("File cannot be created. Do you have Storage Permission?")
            return
        }

        val fileName = Uri.parse(request.url).lastPathSegment

        val filePath = Downloader.path + File.separator + fileName

        val file = File(filePath)

        var urlConnection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var readCounter = 0

        try {
            val url = URL(request.url)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.setRequestProperty("Connection", "Keep-Alive")

            val responseCode = urlConnection.responseCode

            if (responseCode != HttpURLConnection.HTTP_OK) {
                request.status = DownloadStatus.FAILED
                request.isDownloading = false
                Downloader.callback.failure("Download failed: $responseCode")

            } else {

                val contentLength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    urlConnection.contentLengthLong
                } else {
                    urlConnection.contentLength.toLong()
                }

                request.total = contentLength

                inputStream = urlConnection.inputStream

                var bytesRead: Int

                val buffer = ByteArray(BUFFER_SIZE)

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    readCounter++
                    file.appendBytes(buffer)

                    request.downloaded += bytesRead.toLong()

                    if (readCounter % 64 == 0) {
                        when (request.status) {
                            DownloadStatus.CANCELLED -> {
                                Downloader.callback.failure("Download has been cancelled")
                                return
                            }
                            else -> {
                                // updating progress
                                if (readCounter % 16 == 0)
                                    Downloader.callback.progress(
                                        request.downloaded,
                                        request.total
                                    )
                            }
                        }
                    }
                }

                request.isDownloading = false
                Downloader.callback.success()
                inputStream.close()
            }
        } catch (e: IOException) {

            e.printStackTrace()
            request.isDownloading = false
            request.status = DownloadStatus.FAILED
            Downloader.callback.failure("Download failed: $e")

        } finally {
            urlConnection?.disconnect()
            Downloader.curDownloadPos++
            performSequentialDownloads()
        }
    }

}
