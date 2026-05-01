package com.android.masterdistributormdl.gskDistributor.utils

import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.TextView
import com.android.masterdistributormdl.R


object Alert {
    private var dialog: Dialog? = null


    fun hide() {
        if (dialog != null)
            if (dialog!!.isShowing)
                dialog!!.dismiss()
    }

    fun updateApp (context: Context,   msg:String,click: () -> Unit) {
        hide()
        dialog = getDialog(context, R.layout.update_dialog)
        dialog?.setCancelable(false)
        val title = dialog?.findViewById<TextView>(R.id.title)
        val message = dialog?.findViewById<TextView>(R.id.message)
        message?.setText(getHtmlSpanned(msg))
        val update = dialog?.findViewById<Button>(R.id.update)
        dialog?.show()
        title?.text = "Update " + getString(R.string.app_name)
        update?.setOnClickListener {
            hide()
            click()
        }
    }

    fun updateApp(context: Context, click: () -> Unit) {
        hide()
        dialog = getDialog(context, R.layout.update_dialog)
        dialog?.setCancelable(false)
        val title = dialog?.findViewById<TextView>(R.id.title)
        val message = dialog?.findViewById<TextView>(R.id.message)
        val update = dialog?.findViewById<Button>(R.id.update)
        dialog?.show()
        title?.text = "Update " + getString(R.string.app_name)
        update?.setOnClickListener {
            hide()
            click()
        }
    }

}






