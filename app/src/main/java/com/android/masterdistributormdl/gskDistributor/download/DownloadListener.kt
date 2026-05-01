package com.android.masterdistributormdl.gskDistributor.download

import java.io.File

interface DownloadListener {
    fun onFinish(file: File?)
    fun onProgress(progress: Int )
    fun onFailed(error: String)
}