package com.harisewak.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.harisewak.downloader.DownloadStatus
import com.harisewak.downloader.Downloader
import com.harisewak.downloader.Request
import com.harisewak.downloadmanager.R
import com.harisewak.downloadmanager.databinding.ItemDownloadBinding
import com.harisewak.downloadmanager.ui.DownloadListActivity
import javax.inject.Inject

class DownloadListAdapter() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var callback: DownloadListActivity.Callback

    fun setListener(callback: DownloadListActivity.Callback) {
        this.callback = callback
    }


    lateinit var resultsItem: MutableList<Request>

    fun submitList(resultsItem: List<Request>) {
        this.resultsItem = resultsItem.toMutableList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDownloadBinding.inflate(inflater, parent, false)
        return DownloadItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return resultsItem.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as DownloadItemViewHolder
        viewHolder.bindViews(position)
    }

    inner class DownloadItemViewHolder(val binding: ItemDownloadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("CheckResult")
        fun bindViews(position: Int) {
            val item = resultsItem[position]

            with(binding) {
                tvFileName.text = item.fileName
                val status = item.status.toString()
                tvStatus.text = status

                val currProgress = calcProgress(item.downloaded, item.total)
                pbPercentage.progress = currProgress
                tvPercentage.text = "${currProgress}%"

                when (item.status) {
                    DownloadStatus.DOWNLOADED, DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> {
                        pbPercentage.progressDrawable =
                            pbPercentage.context.getDrawable(R.drawable.curved_progress_bar_green)
                    }
                    else -> {
                        pbPercentage.progressDrawable =
                            pbPercentage.context.getDrawable(R.drawable.curved_progress_bar_orange)
                    }
                }

                when (item.status) {

                    DownloadStatus.DOWNLOADED -> {
                        ivCompleted.visibility = View.VISIBLE
                        ivRetry.visibility = View.GONE
                    }
                    DownloadStatus.CANCELLED, DownloadStatus.FAILED -> {
                        ivRetry.visibility = View.VISIBLE
                        ivCompleted.visibility = View.GONE
                    }
                    else -> {
                        ivCompleted.visibility = View.GONE
                        ivRetry.visibility = View.GONE
                    }

                }

                ivRetry.setOnClickListener {
                    callback.retry(item.url)
                }

                ivCancel.setOnClickListener {
                    callback.cancelRequest(item.id!!)
                }


            }

        }

        private fun calcProgress(downloaded: Long, total: Long): Int {
            if (total == 0L) return 0; // Initially total is 0. Adding check to avoid ArithmeticException
            return ((downloaded / total) * 100).toInt()
        }
    }
}