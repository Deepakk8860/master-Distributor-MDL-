package com.android.masterdistributormdl.gskDistributor.utils

import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.TextView
import com.android.masterdistributormdl.R


class SuccessAlert {
    companion object {
        private var mDialog: Dialog? = null

        fun show(
            context: Context,
            message: String,
            click: () -> Unit
        ) {
            var dialog = mDialog
            if (dialog != null)
                if (dialog.isShowing)
                    dialog.dismiss()
            dialog = getDialog(context, R.layout.success_alert)
            dialog.setCancelable(false)
            val msg: TextView = dialog.findViewById(R.id.msg)
            val ok: Button = dialog.findViewById(R.id.ok)
            ok.setOnClickListener {
                dialog.dismiss()
                click()
            }
            msg.text = getHtmlSpanned(message)

            dialog.show()
            mDialog = dialog
        }
    }


}



