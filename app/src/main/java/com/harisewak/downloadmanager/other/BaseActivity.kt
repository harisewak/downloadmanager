package com.harisewak.downloadmanager.other

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val permReqCode = 111
const val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE


abstract class BaseActivity : AppCompatActivity() {

    protected fun isStoragePermissionGranted() = ContextCompat.checkSelfPermission(
        applicationContext,
        storagePermission
    ) == PackageManager.PERMISSION_GRANTED


    protected fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), permReqCode)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permReqCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission granted
            onPermissionGranted()
        } else {
            // permission denied
            if (shouldShowRequestPermissionRationale(storagePermission)) {
                // show rationale for permission
                showPermissionRationale()
            } else {
                // intimate user that permission is necessary for app to run
                showPermissionRequired()
            }
        }
    }


    abstract fun onPermissionGranted()

    abstract fun showPermissionRationale()

    abstract fun showPermissionRequired()

    fun hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}