package com.android.masterdistributormdl.gskDistributor.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class DownloadFileCustom {

    // Entry point to start the download process
    fun download(context: Context, uri: String) {
        val contentUri = Uri.parse(uri)

        val file = createFileInStorage(context)
        if (file.exists()) {
            // If the file already exists, show a message
            showToast(context, "Already downloaded")
        } else {
            // If the file does not exist, proceed with downloading
            openInputStream(context, contentUri)?.let { inputStream ->
                saveFile(inputStream, file)
                showToast(context, "Downloaded successfully")
            } ?: showToast(context, "Failed to open the file")
        }
    }

    // Function to open the InputStream from the content URI
    private fun openInputStream(context: Context, uri: Uri): InputStream? {
        return try {
            context.contentResolver.openInputStream(uri)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Function to create a new file in external storage (Downloads directory)
    private fun createFileInStorage(context: Context): File {
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyAppDownloads")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return File(directory, "downloaded_image.png")  // You can dynamically change the file name
    }

    // Function to save the file by copying the InputStream data to the OutputStream
    private fun saveFile(inputStream: InputStream, file: File) {
        try {
            FileOutputStream(file).use { outputStream ->
                copyStream(inputStream, outputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Function to copy data from InputStream to OutputStream
    private fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        inputStream.close()
        outputStream.flush()
    }

    // Function to show a Toast message
    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
