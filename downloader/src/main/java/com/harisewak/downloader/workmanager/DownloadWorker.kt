package com.harisewak.downloader.workmanager

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.harisewak.downloader.DownloadStatus
import com.harisewak.downloader.R
import com.harisewak.downloader.other.*
import com.harisewak.downloader.room.DatabaseUtil
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import android.app.NotificationManager

import android.app.NotificationChannel


class DownloadWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val requestDao by lazy {
        DatabaseUtil
            .requestDao(appContext)
    }

    var notificationBuilder: NotificationCompat.Builder? = null
    var notificationManager: NotificationManager? = null

    init {
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createForegroundInfo(requestId: Long): ForegroundInfo {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(CHANNEL_NAME)
            .setSmallIcon(R.drawable.ic_work_notification)
            .setOngoing(true)

        return ForegroundInfo(requestId.toInt(), notificationBuilder!!.build())

    }

    private fun showProgressNotification(
        requestId: Long,
        fileName: String,
        progressPercent: String,
        progress: Int,
        notificationManager: NotificationManager?
    ) {

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_work_notification)
            .setContentTitle(fileName)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentText(progressPercent)
            .setProgress(100, progress, false)
            .build()

        notificationManager?.notify(requestId.toInt(), notification)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager?) {

        var notificationChannel =
            notificationManager?.getNotificationChannel(CHANNEL_ID)
        if (notificationChannel == null) {
            notificationChannel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {

        val requestId = inputData.getLong(REQUEST_ID, -1L)

        setForeground(createForegroundInfo(requestId))

        return executeDownloadRequest(requestId)

    }


    //    When executed, request details are updated in db
    suspend fun executeDownloadRequest(requestId: Long): Result {

        if (requestId == -1L) {

            val inputData = Data.Builder()
                .putInt(REASON, REASON_DATABASE_ERROR)
                .putString(MESSAGE, "request ID cannot be negative")
                .build()

            logd("request ID cannot be negative")

            return Result.failure(inputData)
        }

        if (downloadDirPath == null) {

            val inputData = Data.Builder()
                .putInt(REASON, REASON_STORAGE_ERROR)
                .putString(MESSAGE, "Download directory is not accessible")
                .build()

            logd("Download directory is not accessible")

            return Result.failure(inputData)
        }

        val request = requestDao.findById(requestId)

        request.isDownloading = true
        request.status = DownloadStatus.DOWNLOADING

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

            logd("Download directory does not exist!")

            return Result.failure(inputData)
        }

        val fileName = Uri.parse(request.url).lastPathSegment

        request.fileName = fileName

        val filePath = downloadDirPath + File.separator + fileName

        request.filePath = filePath

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

                    val requestToCancel = requestDao.findById(requestId)

                    // check if request was cancelled, cancel download and delete request from database
                    if (requestToCancel.status == DownloadStatus.CANCELLED) {

                        requestToCancel.isDownloading = false

                        requestDao.delete(requestToCancel)

                        val inputData = Data.Builder()
                            .putInt(REASON, REASON_CANCELLED)
                            .build()

                        return Result.failure(inputData)
                    }

                    // updating progress
                    if (readCounter % PROGRESS_UPDATE_INTERVAL == 0) {
                        logd("file name: ${request.fileName}, downloaded: ${request.downloaded}, status: ${request.status}")

                        requestDao.update(
                            requestId,
                            request.downloaded,
                            request.total,
                            request.status
                        )

                        val progress = calcProgress(
                            request.downloaded,
                            request.total
                        )

                        val progressText = "$progress% downloaded"

                        showProgressNotification(
                            requestId,
                            request.fileName!!,
                            progressText,
                            progress,
                            notificationManager
                        )

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

//            val inputData = Data.Builder()
//                .putInt(REASON, REASON_NETWORK_FAILURE)
//                .putString(MESSAGE, e.message)
//                .build()
//
//            return Result.failure(inputData)

            return Result.retry()

        } finally {

            urlConnection?.disconnect()

        }

    }

    val downloadDirPath: String? by lazy {
        appContext.filesDir.absolutePath
    }

    private fun calcProgress(downloaded: Long, total: Long): Int {
        logd("adapter: downloaded -> $downloaded, total -> $total")
        if (total == 0L) return 0; // Initially total is 0. Adding check to avoid ArithmeticException
        return ((downloaded.toFloat() / total.toFloat()) * 100).toInt()
    }

}