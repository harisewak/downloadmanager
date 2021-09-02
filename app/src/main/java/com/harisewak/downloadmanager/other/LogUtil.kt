package com.harisewak.downloadmanager.other

import android.util.Log

const val TAG = "DownloadManager"
const val ENABLED = true

fun logd(message: String) {
    if (ENABLED) {
        Log.d(TAG, message)
    }
}