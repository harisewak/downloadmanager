package com.harisewak.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PhotosVieModel @Inject constructor(
    val photosAdapter: PhotosAdapter
) : ViewModel() {

}