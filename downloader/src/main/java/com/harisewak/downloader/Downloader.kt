package com.harisewak.downloader

import android.content.Context
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.work.*
import com.harisewak.downloader.other.REQUEST_ID
import com.harisewak.downloader.other.RETRY_INTERVAL_MS
import com.harisewak.downloader.other.logd
import com.harisewak.downloader.room.DatabaseUtil
import com.harisewak.downloader.workmanager.DownloadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit



// NOTE: Use a single instance for all operations, in future consider making this class a singleton.
// It requires context for db & workmanager operations hence kept as class for now.

class Downloader(private val context: Context) {

    fun enqueue(url: String) {
        GlobalScope.launch(Dispatchers.IO) {
            download(url)
        }
    }

    private lateinit var listener: Callback

    fun setListener(listener: Callback) {
        this.listener = listener
    }

    // User requests a download
    private suspend fun download(url: String) {

        // check for existing request
        val existingRequest = DatabaseUtil.requestDao(context).findByUrl(url)

        // if existing request is in queued or downloading state, skip it
        if (existingRequest != null && (existingRequest.status == DownloadStatus.QUEUED || existingRequest.status == DownloadStatus.DOWNLOADING)) {
            listener.onError(context.getString(R.string.msg_request_exists))
            return
        }

        // existing request is in cancelled or failed state, retry
        if (existingRequest != null && (existingRequest.status == DownloadStatus.CANCELLED || existingRequest.status == DownloadStatus.FAILED)) {
            // cancel existing work request and delete entry from database
            WorkManager.getInstance(context)
                .cancelWorkById(UUID.fromString(existingRequest.workRequestId!!))

            DatabaseUtil.requestDao(context).delete(existingRequest)
        }

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
            .setBackoffCriteria(BackoffPolicy.LINEAR, RETRY_INTERVAL_MS, TimeUnit.MILLISECONDS)
            .build()

        // saving work request ID in database to re-schedule it whenever required
        val updateRequest = DatabaseUtil.requestDao(context).findById(requestId)
        updateRequest.workRequestId = workRequest.id.toString()

        logd("workRequestId: ${updateRequest.workRequestId}")

        DatabaseUtil.requestDao(context).update(updateRequest)

        WorkManager.getInstance(context)
            .enqueue(workRequest)

    }

    fun cancelRequest(id: Long) {

        GlobalScope.launch(Dispatchers.IO) {

            val request = DatabaseUtil.requestDao(context).findById(id)

            // if request is ongoing, just update status and cancellation will be handled in DownloadWorker
            // else delete request from database. Finally remove work request from WorkManager.

            if (request.status == DownloadStatus.DOWNLOADING) {
                request.status = DownloadStatus.CANCELLED
                DatabaseUtil.requestDao(context).update(request)
            } else {
                DatabaseUtil.requestDao(context).delete(request)
            }

            WorkManager.getInstance(context)
                .cancelWorkById(UUID.fromString(request.workRequestId!!))

        }
    }


    private val downloadList = DatabaseUtil.requestDao(context).getAll().asLiveData()


    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<List<Request>>) {
        downloadList.observe(lifecycleOwner, observer)
    }



    // Callback events about an initiated download
    interface Callback {
        fun onError(message: String)
    }
}