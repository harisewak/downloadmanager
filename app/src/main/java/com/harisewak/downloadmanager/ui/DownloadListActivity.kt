package com.harisewak.downloadmanager.ui

import android.os.Bundle
import android.webkit.URLUtil
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.harisewak.downloader.Downloader
import com.harisewak.downloadmanager.databinding.ActivityDownloadListBinding
import com.harisewak.ui.DownloadListAdapter
import com.harisewak.ui.DownloadListViewModel
import com.harisewak.ui.Status
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.view.Gravity

import android.widget.FrameLayout

import com.google.android.material.snackbar.Snackbar
import com.harisewak.downloadmanager.R


@AndroidEntryPoint
class DownloadListActivity : AppCompatActivity() {

    private val viewModel: DownloadListViewModel by viewModels()
    private lateinit var binding: ActivityDownloadListBinding

    @Inject
    lateinit var downloadListAdapter: DownloadListAdapter

    @Inject
    lateinit var downloader: Downloader


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()

        setDummyData()
    }

    private fun setListeners() {

        binding.etUrl.setOnEditorActionListener { _, _, _ ->
            downloadClicked(binding.etUrl.text.toString())
            true
        }

        binding.btDownload.setOnClickListener { downloadClicked(binding.etUrl.text.toString()) }

    }

    private fun downloadClicked(inputText: String) {
        // verify download url
        if (URLUtil.isValidUrl(inputText)) {
            // pass it to downloader
            downloader.enqueue(inputText)
        } else {
            showSnackbar(getString(R.string.error_invalid_url))
        }
    }

    private fun setDummyData() {

        val list = mutableListOf<Status>()
        list.add(Status("My File name.mp4", PROGRESS, 50))
        list.add(Status("My File name.txt", PROGRESS, 10))
        list.add(Status("My File name.mp3", FAILED, 90))

        downloadListAdapter.setStatus(list)
        binding.rvDownloads.adapter = downloadListAdapter
    }

    fun showSnackbar(message: String) {
        val snack: Snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snack.show()
    }


    companion object {
        const val PROGRESS = 1
        const val FAILED = 2
    }
}