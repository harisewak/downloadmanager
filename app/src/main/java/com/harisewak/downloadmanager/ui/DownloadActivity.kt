package com.harisewak.downloadmanager.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.harisewak.downloadmanager.databinding.ActivityDownBinding
import com.harisewak.ui.PhotosVieModel
import com.harisewak.ui.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadActivity:AppCompatActivity() {

    private val viewModel: PhotosVieModel by viewModels()
    private lateinit var binding: ActivityDownBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setDummyData()
    }

    private fun setDummyData() {

        val list= mutableListOf<Status>()
        list.add(Status("My File name.mp4", PROGRESS,50))
        list.add(Status("My File name.txt", PROGRESS,10))
        list.add(Status("My File name.mp3", FAILED,90))

        viewModel.photosAdapter.setStatus(list)
        binding. rvPhotos.adapter = viewModel.photosAdapter
    }


    companion object{
        const val PROGRESS=1
        const val FAILED=2
    }
}