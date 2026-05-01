package com.android.masterdistributormdl.gskDistributor.utils

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.android.masterdistributormdl.BuildConfig
import java.io.File


object FileOpen {

    fun shareFIle(context: Context, file: File) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/pdf"
        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(Intent.createChooser(shareIntent, "Share Shop Certificate"))
    }


    fun openFile(context: Context, file: File) {
        try {
            val uri: Uri
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                intent.clipData = ClipData.newRawUri("", uri)
            } else {
                uri = Uri.fromFile(file)
            }
            Log.d(TAG, "openFile:File $file")
            if (file.toString().contains(".doc") || file.toString().contains(".docx")) {
                intent.setDataAndType(uri, "application/msword")
            } else if (file.toString().contains(".pdf")) {
                intent.setDataAndType(uri, "application/pdf")
            }
            else if (file.toString().contains(".csv")) {
                intent.setDataAndType(uri, "text/csv")
            }
            else if (file.toString().contains(".ppt") || file.toString().contains(".pptx")) {
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
            } else if (file.toString().contains(".xls") || file.toString().contains(".xlsx")) {
                intent.setDataAndType(uri, "application/vnd.ms-excel")
            } else if (file.toString().contains(".zip")) {
                intent.setDataAndType(uri, "application/zip")
            } else if (file.toString().contains(".rar")) {
                intent.setDataAndType(uri, "application/x-rar-compressed")
            } else if (file.toString().contains(".rtf")) {
                intent.setDataAndType(uri, "application/rtf")
            } else if (file.toString().contains(".wav") || file.toString().contains(".mp3")) {
                intent.setDataAndType(uri, "audio/x-wav")
            } else if (file.toString().contains(".gif")) {
                intent.setDataAndType(uri, "image/gif")
            } else if (file.toString().contains(".jpg") || file.toString()
                    .contains(".jpeg") || file.toString().contains(".png")
            ) {
                intent.setDataAndType(uri, "image/jpeg")
            } else if (file.toString().contains(".txt")) {
                intent.setDataAndType(uri, "text/plain")
            } else if (file.toString().contains(".3gp") || file.toString()
                    .contains(".mpg") || file.toString().contains(".mpeg") || file.toString()
                    .contains(".mpe") || file.toString().contains(".mp4") || file.toString()
                    .contains(".avi")
            ) {
                intent.setDataAndType(uri, "video/*")
            } else if (file.toString().contains(".apk")) {
                installApp(context, file)
                return
            } else {
                intent.setDataAndType(uri, "*/*")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context, "No application found which can open the file", Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun installApp(context: Context, file: File) {
        val install = Intent(Intent.ACTION_VIEW)
        install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            val uri = FileProvider.getUriForFile(
                context, BuildConfig.APPLICATION_ID + ".provider", file
            )
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            install.data = uri
            context.startActivity(install)
        } else {
            val appInstallPath = "application/vnd.android.package-archive"
            install.setDataAndType(Uri.fromFile(file), appInstallPath)
            context.startActivity(install)

        }
    }


}