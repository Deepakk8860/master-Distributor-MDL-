package com.android.masterdistributormdl.gskDistributor.download

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.gskDistributor.utils.getDialog


class ProgressLoading(val context: Context) {
    private var dialog: Dialog? = null
    private var title: TextView? = null
    private var percent: TextView? = null
    private var progressBar: ProgressBar? = null

    init {
        initilize()
    }

    fun setTitle(title: String) {
        this.title?.text = title
    }

    @SuppressLint("SetTextI18n")
    fun setProgress(progress: Int) {
        progressBar?.progress = progress
        percent?.text = "$progress%"
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    fun hide() {
        dialog?.dismiss()
    }

    fun setMessage(message: String) {
        title?.text = message
    }

    fun initilize() {
        dialog = getDialog(context, R.layout.progress_loading)
        dialog?.setCancelable(false)
        title = dialog?.findViewById(R.id.title)
        progressBar = dialog?.findViewById(R.id.progressBar)
        percent = dialog?.findViewById(R.id.percent)
        title?.text = "Downloading"
        percent?.text = "0%"
        progressBar?.progress = 2
        dialog?.show()
    }

}



