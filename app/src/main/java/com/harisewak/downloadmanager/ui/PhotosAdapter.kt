package com.harisewak.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.harisewak.downloadmanager.R
import com.harisewak.downloadmanager.databinding.ItemDownloadingFileBinding
import com.harisewak.downloadmanager.ui.DownloadActivity.Companion.PROGRESS
import javax.inject.Inject

class PhotosAdapter @Inject constructor() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var resultsItem: MutableList<Status>

    fun setStatus(resultsItem: List<Status>) {
        this.resultsItem = resultsItem.toMutableList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDownloadingFileBinding.inflate(inflater, parent, false)
        return PhotoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return resultsItem.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as PhotoViewHolder
        viewHolder.bindViews(position)
    }

    inner class PhotoViewHolder(val binding: ItemDownloadingFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("CheckResult")
        fun bindViews(position: Int) {
            val items = resultsItem[position]
            binding.apply {

                tvFileName.text=items.fileName
                val status=if (items.status==PROGRESS) "Downloading" else "Failed"
                tvStatus.text=status
                pbPercentage.progress=items.progress
                tvPercentage.text="${items.progress}%"
                if (items.status==PROGRESS) {
                    pbPercentage.progressDrawable=pbPercentage.context.getDrawable(R.drawable.curved_progress_bar_green)
                }
                else{
                    pbPercentage.progressDrawable=pbPercentage.context.getDrawable(R.drawable.curved_progress_bar_orange)
                }
            }

        }
    }
}