package com.android.masterdistributormdl.gskDistributor.utils

import android.app.Dialog
import android.content.Context
import android.view.Window

import android.widget.ImageView
import com.android.masterdistributormdl.R
import com.squareup.picasso.Picasso


class ImageDialog(val context: Context, url: String) {
    init {
        val dialog = Dialog(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawableResource(R.color.trans)
        dialog.setContentView(R.layout.image_dialog)
        val imageView = dialog.findViewById<ImageView>(R.id.imageView)
        val back = dialog.findViewById<ImageView>(R.id.back)

        back.setOnClickListener {
            dialog.dismiss()
        }
        Picasso.get()
            .load(url)
            .into(imageView)
        dialog.show()
    }


}

/*
class ChildProgressDialog(val context: Context, val data: String) {
    init {
        val dialog = Dialog(context, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawableResource(R.color.trans)
        dialog.setContentView(R.layout.child_progress_dialog)
        val webView = dialog.findViewById<WebView>(R.id.webView)
        val back = dialog.findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowContentAccess = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.loadsImagesAutomatically = true
        webSettings.defaultTextEncodingName = "utf-8"
        //webSettings.textSize = WebSettings.TextSize.NORMAL
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = false
        webSettings.loadWithOverviewMode = true
        webSettings.setAppCacheEnabled(true)
        webView.webChromeClient = WebChromeClient()
        webView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
    }


}*/
