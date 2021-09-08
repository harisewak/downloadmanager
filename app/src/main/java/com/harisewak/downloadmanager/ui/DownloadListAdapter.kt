package com.harisewak.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.harisewak.downloader.DownloadStatus
import com.harisewak.downloader.Request
import com.harisewak.downloadmanager.R
import com.harisewak.downloadmanager.databinding.ItemDownloadBinding
import com.harisewak.downloadmanager.other.logd
import com.harisewak.downloadmanager.ui.DownloadListActivity

class DownloadListAdapter :
    ListAdapter<Request, DownloadListAdapter.DownloadItemViewHolder>(DIFF_CALLBACK) {

    companion object {

        val DIFF_CALLBACK =

            object : DiffUtil.ItemCallback<Request>() {

                override
                fun areItemsTheSame(
                    oldRequest: Request, newRequest: Request
                ): Boolean {
                    // Request properties may have changed if reloaded from the DB, but ID is fixed
                    return oldRequest.id == newRequest.id;
                }

                override
                fun areContentsTheSame(
                    oldRequest: Request, newRequest: Request
                ): Boolean {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldRequest.equals(newRequest);
                }
            }
    }

    private lateinit var callback: DownloadListActivity.Callback

    fun setListener(callback: DownloadListActivity.Callback) {
        this.callback = callback
    }



    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DownloadItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDownloadBinding.inflate(inflater, parent, false)
        return DownloadItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class DownloadItemViewHolder(val binding: ItemDownloadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("CheckResult")
        fun bind(position: Int) {
            val item = currentList[position]

            with(binding) {
                tvFileName.text = item.fileName
                val status = item.status.toString()
                tvStatus.text = status

                val currProgress = calcProgress(item.downloaded, item.total)
                pbPercentage.progress = currProgress
                tvPercentage.text = "${currProgress}%"

                logd("adapter: download progress -> $currProgress")

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
            logd("adapter: downloaded -> $downloaded, total -> $total")
            if (total == 0L) return 0; // Initially total is 0. Adding check to avoid ArithmeticException
            return ((downloaded.toFloat() / total.toFloat()) * 100).toInt()
        }
    }
}