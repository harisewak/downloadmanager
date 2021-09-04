package com.harisewak.downloader.other

import android.util.Log

const val TAG = "Downloader"
const val ENABLED = true

fun logd(message: String) {
    if (ENABLED) {
        Log.d(TAG, message)
    }
}