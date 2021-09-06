package com.harisewak.downloader

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.harisewak.downloader.other.TAG

class AppLifecycleObserver(private val lifecycle: Lifecycle): LifecycleObserver {

    private var status = DISCONNECTED

    init {
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        status = CONNECTED
        Log.d(TAG, "start: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        status = DISCONNECTED
        Log.d(TAG, "stop: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        lifecycle.removeObserver(this)
        Log.d(TAG, "destroy: ")
    }

    companion object {
        const val CONNECTED = "Connected"
        const val DISCONNECTED = "Disconnected"
    }

}