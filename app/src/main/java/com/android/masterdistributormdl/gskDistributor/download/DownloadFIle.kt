package com.android.masterdistributormdl.gskDistributor.download

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.android.masterdistributormdl.BuildConfig
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.FileOpen
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Utils
import com.android.masterdistributormdl.gskDistributor.utils.getDialog
import com.android.masterdistributormdl.gskDistributor.utils.isNetworkAvailable
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.google.gson.JsonObject

import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File

class DownloadFIle {

    fun download(
        context: Context, url: String, param: JsonObject? = null, fileName: String? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            createFIle(context, url, param, fileName)
        } else {
            requestPermissions(context, url, param, fileName)
        }

    }


    private fun requestPermissions(
        context: Context, url: String, param: JsonObject?, fileName: String?
    ) {
        val dexter = Dexter.withContext(context)
        val withListener = dexter.withPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        )

        withListener.withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                    createFIle(context, url, param, fileName)
                } else if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied) {
                    Utils.openAppSetting(
                        context, "Please grant permission for download file"
                    )
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                list: List<PermissionRequest?>?, permissionToken: PermissionToken
            ) {
                permissionToken.continuePermissionRequest()
            }
        }).withErrorListener { error: DexterError? ->
            showToastShort("Error occurred! ")
        }.onSameThread().check()
    }

    private fun createFIle(context: Context, url: String, param: JsonObject?, fileName: String?) {
        val fileName2: String
        val appName = context.getString(R.string.app_name) + "_"
        if (fileName.isNullOrEmpty()) {
            fileName2 = appName + url.substring(url.lastIndexOf('/') + 1)
        } else {
            fileName2 = appName + fileName
        }

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName2
        )
          /* if (file.exists()) {
               fileExitsAlert(context, url, param, file)
           } else {
               startDownloading(context, url, param, file)
           }*/
        startDownloading(context, url, param, file)
    }


    private fun fileExitsAlert(context: Context, url: String, param: JsonObject?, file: File) {
        val dialog = getDialog(context, R.layout.file_alert)
        val close = dialog.findViewById<ImageView>(R.id.close)
        val msg = dialog.findViewById<TextView>(R.id.msg)

        val open = dialog.findViewById<Button>(R.id.open)
        val download = dialog.findViewById<Button>(R.id.download)
        close.setOnClickListener {
            dialog.dismiss()
        }
        download.setOnClickListener {
            dialog.dismiss()

            if (file.exists())
                file.delete()
            startDownloading(context, url, param, file)
        }
        open.setOnClickListener {
            dialog.dismiss()
            FileOpen.openFile(context, file)
        }
        dialog.show()

    }

    private fun startDownloading(
        context: Context, url: String, param: JsonObject? = null, file: File
    ) {
        if (!isNetworkAvailable(context)) {
            InternetError.show(context)
            return
        }

        val progressDialog = ProgressLoading(context)
        progressDialog.setTitle("Downloading")
        val util = DownloadUtil.getInstance(context)
        util.downloadFile(url, param, file, true, object : DownloadListener {
            override fun onFinish(file: File?) {
                progressDialog.hide()
                if (file != null) {
                    if (file.exists()) {
                        FileOpen.openFile(context, file)
                    } else {
                        showToastShort("File not downloaded")
                    }
                }
            }

            override fun onProgress(progress: Int) {
                progressDialog.setProgress(progress)
            }

            override fun onFailed(error: String) {
                progressDialog.hide()
                if (BuildConfig.DEBUG) {
                    AlertError.show(context, error) {}
                } else {
                    showToastShort(error)
                }
            }
        })
    }


}