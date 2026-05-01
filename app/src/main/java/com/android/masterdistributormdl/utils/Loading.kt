package com.android.masterdistributormdl.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager
import com.android.masterdistributormdl.R


object Loading {
    private var dialog: Dialog? = null

    fun showHide(context: Context, it: Boolean) {
        if (it) show(context) else dismiss()
    }

    fun showHide2(context: Context, it: Boolean) {
        if (it) show2(context) else dismiss()
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    fun show(context: Context) {
        dismiss()
        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.loading1)
        val window = dialog?.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.show()
    }

    fun show2(context: Context) {
        dismiss()
        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.loading2)
        val window = dialog?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.show()
    }
}



