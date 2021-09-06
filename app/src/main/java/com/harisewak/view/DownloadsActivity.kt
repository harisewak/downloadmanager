package com.harisewak.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.harisewak.downloader.Callback
import com.harisewak.downloader.Downloader
import com.harisewak.downloadmanager.databinding.ActivityDownloadsBinding
import com.harisewak.downloadmanager.other.logd

class DownloadsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadsBinding

    private val downloader by lazy {
        Downloader(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDownloadsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btDownload.setOnClickListener {

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

            downloader.enqueue(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            )

            downloader.enqueue(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            )

            downloader.enqueue(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            )

//            Downloader.with(
//                applicationContext,
//                callback
//            ).enqueue(
//                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
//            )
//
//            Downloader.with(
//                applicationContext,
//                callback
//            ).enqueue(
//                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
//            )
//
//            Downloader.with(
//                applicationContext,
//                callback
//            ).enqueue(
//                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
//            )


        }


    }
}