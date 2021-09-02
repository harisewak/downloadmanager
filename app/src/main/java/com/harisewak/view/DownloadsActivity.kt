package com.harisewak.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.harisewak.downloadmanager.R
import com.harisewak.downloadmanager.databinding.ActivityDownloadsBinding
import com.harisewak.downloadmanager.downloader.Callback
import com.harisewak.downloadmanager.downloader.Downloader
import com.harisewak.downloadmanager.other.logd

class DownloadsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDownloadsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btDownload.setOnClickListener {

            val filePath = filesDir.absolutePath

            val callback = object : Callback {
                override fun progress(progress: Long, total: Long) {
                    logd("progress called: progress - $progress, total - $total")
                }

                override fun success() {
                    logd("success called")
                }

                override fun failure(message: String) {
                    logd("failure called: message - $message")
                }

            }

            // Making multiple requests

            Downloader.download(
                filePath,
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                callback
            )

            Downloader.download(
                filePath,
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                callback
            )

            Downloader.download(
                filePath,
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                callback
            )


        }
    }
}