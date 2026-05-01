package com.android.masterdistributormdl.gskDistributor.download

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.utils.BASE_URL
import com.android.masterdistributormdl.gskDistributor.utils.FileUtil
import com.android.masterdistributormdl.gskDistributor.utils.getStringBodyFromJson
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DownloadUtil(private val context: Context) {
    companion object {
        fun getInstance(context: Context): DownloadUtil {
            return DownloadUtil(context)
        }
    }

    private val executorService = Executors.newSingleThreadExecutor()
    private val executor = ThreadExecutor()
    private val logInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private fun getRetrofit(listener: DownloadListener): DownloadService {
        val downloadInterceptor = DownloadInterceptor(listener)
        val builder = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(downloadInterceptor)
            .addInterceptor(logInterceptor)
            .cache(null)
            .build()
        return Retrofit.Builder()
            .client(builder)
            .baseUrl(BASE_URL).build()
            .create(DownloadService::class.java)
    }

    fun downloadFile(
        url: String,
        param: JsonObject?,
        file: File?,
        uiThread: Boolean,
        listener: DownloadListener
    ) {

        val service = getRetrofit(listener)
        executorService.execute {
            try {
                val result: Response<ResponseBody> = if (param == null) {
                    service.downloadUrl(url).execute()
                } else {
                    val body = getStringBodyFromJson(param)
                    service.downloadUrl(url, body).execute()
                }
                val file2 = writeFile(context, result.body()!!, file!!)
                if (uiThread) {
                    executor.execute { listener.onFinish(file2) }
                } else {
                    listener.onFinish(file2)
                }
            } catch (e: Exception) {
                val error = e.localizedMessage?.toString().toString()
                if (uiThread) {
                    executor.execute { listener.onFailed(error) }
                } else {
                    listener.onFailed(error)
                }
            }
        }
    }

    fun writeFile(
        context: Context,
        body: ResponseBody,
        file: File,
    ): File? {

        val contentResolver = context.contentResolver
        var inputStream: InputStream? = body.byteStream()
        var outputStream: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val values = ContentValues().apply {
                val name = file.name
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                // put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            var parcelFileDescriptor: ParcelFileDescriptor? = null
            try {
                parcelFileDescriptor = uri?.let { contentResolver.openFileDescriptor(it, "w") }!!
                outputStream = FileOutputStream(parcelFileDescriptor.fileDescriptor)
                val buffer = ByteArray(1024 * 4)
                var bytes = inputStream!!.read(buffer)
                while (bytes >= 0) {
                    outputStream.write(buffer, 0, bytes)
                    bytes = inputStream.read(buffer)
                }
                return FileUtil.from(context, uri)
            } catch (e: IOException) {
                e.printStackTrace()
                values.clear()
                if (uri != null) {
                    context.contentResolver.delete(uri, null, null)
                }
            } finally {
                outputStream?.flush()
                inputStream?.close()
                parcelFileDescriptor?.close()
                outputStream?.close()
            }
        } else {
            try {
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file.absolutePath)
                val buffer = ByteArray(1024 * 4)
                var bytes = inputStream.read(buffer)
                while (bytes >= 0) {
                    outputStream.write(buffer, 0, bytes)
                    bytes = inputStream.read(buffer)
                }
                return file
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                outputStream?.flush()
                inputStream?.close()
                outputStream?.close()
            }

        }
        return null
    }
}