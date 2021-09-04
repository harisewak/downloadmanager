package com.harisewak.downloadmanager.downloader

// Download request
class Request(
    val url: String,
    val path: String,
    ) {
    var status: DownloadStatus
    var total: Long = 0
    var downloaded: Long = 0
    var isDownloading = false

    init {
        status = DownloadStatus.QUEUED
    }
}

enum class DownloadStatus {
    QUEUED, DOWNLOADING, DOWNLOADED, CANCELLED, FAILED
}