package com.harisewak.downloadmanager.ui

import android.os.Build
import android.os.Bundle
import android.webkit.URLUtil
import androidx.activity.viewModels
import com.harisewak.downloader.Downloader
import com.harisewak.downloadmanager.databinding.ActivityDownloadListBinding
import com.harisewak.ui.DownloadListAdapter
import com.harisewak.ui.DownloadListViewModel
import com.harisewak.ui.Status
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.view.Gravity

import android.widget.FrameLayout
import androidx.annotation.RequiresApi

import com.google.android.material.snackbar.Snackbar
import com.harisewak.downloadmanager.R
import com.harisewak.downloadmanager.other.BaseActivity
import com.harisewak.downloadmanager.other.logd


@AndroidEntryPoint
class DownloadListActivity : BaseActivity() {

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

        observeData()
    }

    private fun observeData() {

        downloader.observe(this, { downloads ->
            downloads.forEach {
                logd("download: ${it.filePath}, workRequestId: ${it.workRequestId}")
            }

            downloadListAdapter.submitList(downloads)
            binding.rvDownloads.adapter = downloadListAdapter
        })

    }

    private fun setListeners() {

        binding.etUrl.setOnEditorActionListener { _, _, _ ->
            downloadClicked(binding.etUrl.text.toString())
            true
        }

        binding.btDownload.setOnClickListener { downloadClicked(binding.etUrl.text.toString()) }

        downloader.setListener(object : Downloader.Callback {

            override fun onError(message: String) {
                showSnackbar(message)
            }

        })


        downloadListAdapter.setListener(object : Callback {

            override fun cancelRequest(id: Long) {
                downloader.cancelRequest(id)
            }

            override fun retry(url: String) {
                downloader.enqueue(url)
            }

        })

    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionGranted() {
        downloadClicked(binding.etUrl.text.toString())
    }

    override fun showPermissionRationale() {
        showSnackbar(getString(R.string.msg_storage_permission_rationale))
    }

    override fun showPermissionRequired() {
        showSnackbar(getString(R.string.msg_storage_permission_required))
    }


    private fun downloadClicked(inputText: String) {

        if (isStoragePermissionGranted()) {

            if (URLUtil.isValidUrl(inputText)) {

                downloader.enqueue(inputText)

                binding.etUrl.text.clear()

                hideKeyboard()

            } else {

                showSnackbar(getString(R.string.error_invalid_url))

            }

        } else {
            requestStoragePermission()
        }


    }

    private fun setDummyData() {

        val list = mutableListOf<Status>()
        list.add(Status("My File name.mp4", PROGRESS, 50))
        list.add(Status("My File name.txt", PROGRESS, 10))
        list.add(Status("My File name.mp3", FAILED, 90))

//        downloadListAdapter.setStatus(list)
//        binding.rvDownloads.adapter = downloadListAdapter
    }

    fun showSnackbar(message: String) {
        val snack: Snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snack.show()
    }


    // listener for callbacks from adapter
    interface Callback {
        fun cancelRequest(id: Long)
        fun retry(url: String)
    }


    companion object {
        const val PROGRESS = 1
        const val FAILED = 2
    }
}