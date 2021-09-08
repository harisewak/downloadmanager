package com.harisewak.downloadmanager.other

import android.Manifest
import android.Manifest.permission
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
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.os.Build.VERSION

import android.os.Environment

import android.os.Build.VERSION.SDK_INT
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE

import androidx.core.app.ActivityCompat.startActivityForResult

import android.content.Intent

import android.net.Uri

import android.os.Build.VERSION.SDK_INT
import android.provider.Settings
import java.lang.Exception
import android.widget.Toast

import android.os.Build.VERSION.SDK_INT
import androidx.annotation.Nullable


const val permReqCode = 111
const val manageStoragePermCode = 999
const val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE


abstract class BaseActivity : AppCompatActivity() {

    protected fun isStoragePermissionGranted(): Boolean {

        return if (SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {

            val readPerm =
                ContextCompat.checkSelfPermission(this@BaseActivity, READ_EXTERNAL_STORAGE)
            val writePerm =
                ContextCompat.checkSelfPermission(this@BaseActivity, WRITE_EXTERNAL_STORAGE)

            readPerm == PackageManager.PERMISSION_GRANTED && writePerm == PackageManager.PERMISSION_GRANTED
        }

    }


    protected fun requestStoragePermission() {

        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, manageStoragePermCode)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, manageStoragePermCode)
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this@BaseActivity,
                arrayOf(WRITE_EXTERNAL_STORAGE),
                permReqCode
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
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
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}