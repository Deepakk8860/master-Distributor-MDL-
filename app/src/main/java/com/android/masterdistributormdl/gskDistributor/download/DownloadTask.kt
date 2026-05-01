package com.android.masterdistributormdl.gskDistributor.download

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.CoroutineContext


class DownloadTask(
    val context: Context,
    val url: String,
    val file: File,
    private val downLoadListener: DownloadListener
) : CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job


    fun cancel() {
        job.cancel()
    }

    init {
        execute()
    }

    fun execute() = launch {
        onPreExecute()
        val result = doInBackground()
        onPostExecute(result)
    }

    private suspend fun doInBackground(): String = withContext(Dispatchers.IO) {
        val result = download()
        return@withContext result
    }


    private fun onPreExecute() {
        Log.d(TAG, "Downloader onPreExecute  ")

    }

    private fun onPostExecute(result: String) {
        Log.d(TAG, "Downloader onPostExecute")
        if (result.equals("Success"))
            downLoadListener.onFinish(file)
        else {
            downLoadListener.onFailed("Downloading Error : $result")
        }
    }

    private fun publishProgress(progress: Int) {
        Log.d(TAG, "Downloader publishProgress $progress")
        downLoadListener.onProgress(progress)
    }

    fun download(): String {
        var result = ""
        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null
        try {
            val url = URL(url)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val length = connection.getContentLength()
                input = connection.inputStream
                if (file.exists()) file.delete()
                output = FileOutputStream(file)
                val data = ByteArray(4096)
                var total: Int = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    total += count
                    if (length > 0) {
                        updateProgress(total, length)
                    }
                    output.write(data, 0, count)
                }
                result = "Success"
            } else {
                result = "Error : " + connection.getResponseMessage()
            }
        } catch (e: Exception) {
            if (file.exists())
                file.delete()
            result = "" + e.localizedMessage
        } finally {
            output?.close()
            input?.close()
            connection?.disconnect()
        }
        return result
    }

    fun updateProgress(total: Int, length: Int) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            publishProgress((total * 100 / length))
        }
    }
}