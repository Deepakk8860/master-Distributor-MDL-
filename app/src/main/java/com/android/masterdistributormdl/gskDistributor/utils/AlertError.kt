package com.android.masterdistributormdl.gskDistributor.utils

import android.app.Dialog
import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.android.masterdistributormdl.R


object AlertError {
    private var mDialog: Dialog? = null


    fun hide() {
        if (mDialog != null)
            if (mDialog!!.isShowing)
                mDialog!!.dismiss()
    }

    fun show(
        context: Context,
        message: String,
        click: () -> Unit
    ) {
        val dialog = getDialog(context, R.layout.alert_error_dialog)
        dialog.setCancelable(false)
        val msg = dialog.findViewById<TextView>(R.id.msg)
        val close = dialog.findViewById<ImageView>(R.id.close)
        close.setOnClickListener {
            dialog.dismiss()
            click()
        }
        msg.text = getHtmlSpanned(message)
        dialog.show()
        mDialog = dialog
    }
}






