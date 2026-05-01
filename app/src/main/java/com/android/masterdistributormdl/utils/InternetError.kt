package com.android.masterdistributormdl.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import com.android.masterdistributormdl.R

object InternetError {
    private var mDialog: Dialog? = null

    fun hide() {
        if (mDialog != null)
            if (mDialog!!.isShowing)
                mDialog!!.dismiss()
    }

    fun show(context: Context) {
        show(context, null)
    }

    fun show(context: Context, click: (() -> Unit)?) {
        hide()
        val dialog = getDialog(context, R.layout.intenet_error)
        dialog.setCancelable(false)
        val msg = dialog.findViewById<TextView>(R.id.msg)
        val setting = dialog.findViewById<Button>(R.id.setting)
        setting.setOnClickListener {
            hide()
            click?.invoke()
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent);
        }
        val message = context.getString(R.string.internet_error)
        msg.text = getHtmlSpanned(message)
        dialog.show()
        mDialog = dialog
    }
}