package com.android.masterdistributormdl.gskDistributor.view

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ContentBinding
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR1
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.utils.SharedPreference


class Content : Fragment() {
    private lateinit var binding: ContentBinding
    lateinit var model: ContentModel
    lateinit var title: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.content, container, false)
        model = ViewModelProvider(this)[ContentModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setHeader()
        }
    }

    private fun setHeader() {
        try {
            shooterFragment = this
            (activity as MainActivity).setHeader("", STATUS_COLOR1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        initView()
        binding.back.setOnClickListener { requireActivity().onBackPressed() }
        val webSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowContentAccess = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.loadsImagesAutomatically = true
        webSettings.defaultTextEncodingName = "utf-8"
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = false
        webSettings.loadWithOverviewMode = true

        webSettings.blockNetworkImage = true
        webSettings.setNeedInitialFocus(false)
        binding.webView.webViewClient = MyWebViewClient()
        binding.webView.webChromeClient = WebChromeClient()
        val url = requireArguments().getString("url")!!
        binding.webView.loadUrl(url)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.webView.loadUrl(url)
        }
    }

    private fun initView() {
        title = requireArguments().getString("title")!!
        binding.txtTitle.text=title
    }


    inner class MyWebViewClient : WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
           if (url.startsWith("http")) {
                view.loadUrl(url)
            } else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
            return true
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
//            showToast("Got Error! ${error?.description.toString()}")

        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (binding.mainLayout != null) {
                binding.swipeRefreshLayout.isRefreshing = true
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            if (binding.mainLayout != null) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

}


class ContentModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
}

