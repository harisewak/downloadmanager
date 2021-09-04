package com.harisewak.downloadmanager.downloader

import android.net.Uri
import android.os.Build
import com.harisewak.downloader.Callback
import com.harisewak.downloader.other.logd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

const val BUFFER_SIZE = 4096

/* Request, Callback, Downloader*/
object Downloader {
    private lateinit var callback: Callback
    private val downloadQueue: ArrayList<Request> = arrayListOf()
    private var curDownloadPos = -1

    fun enqueue(path: String, urlStr: String, callback: Callback) {

        this.callback = callback

        val request = Request(urlStr, path)
        downloadQueue.add(request)

        if (downloadQueue.size == 1) {
            curDownloadPos = 0

            GlobalScope.launch(Dispatchers.IO) {
                processRequest()
            }

        }

    }

    fun processRequest() {

        // exit if all requests have been consumed
        if (curDownloadPos >= downloadQueue.size) {
            return
        }

        val request = downloadQueue[curDownloadPos]

        logd("processing request: ${request.url}")

        request.isDownloading = true

        val path = File(request.path)

        if (!path.exists()) {
            callback.failure("File cannot be created. Do you have Storage Permission?")
            return
        }

        val fileName = Uri.parse(request.url).lastPathSegment

        val filePath = request.path + File.separator + fileName

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
                callback.failure("Download failed: $responseCode")

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
                                callback.failure("Download has been cancelled")
                                return
                            }
                            else -> {
                                // updating progress
                                if (readCounter % 16 == 0)
                                    callback.progress(
                                        request.downloaded,
                                        request.total
                                    )
                            }
                        }
                    }
                }

                request.isDownloading = false
                callback.success()
                inputStream.close()
            }
        } catch (e: IOException) {

            e.printStackTrace()
            request.isDownloading = false
            request.status = DownloadStatus.FAILED
            callback.failure("Download failed: $e")

        } finally {
            urlConnection?.disconnect()
            curDownloadPos++
            processRequest()
        }
    }
}