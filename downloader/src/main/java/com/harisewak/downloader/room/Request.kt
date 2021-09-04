package com.harisewak.downloader

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

// Download request
@Entity
data class Request(
    @PrimaryKey(autoGenerate = true) private var id: Long = -1, // default value added to avoid passing it while creating request. Should NEVER be used.
    val url: String,
    var status: DownloadStatus = DownloadStatus.QUEUED,
    var isDownloading: Boolean = false,
    var total: Long = 0,
    var downloaded: Long = 0,
)

enum class DownloadStatus {
    QUEUED, DOWNLOADING, DOWNLOADED, CANCELLED, FAILED
}