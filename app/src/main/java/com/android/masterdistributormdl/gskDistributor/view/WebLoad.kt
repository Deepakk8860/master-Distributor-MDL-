package com.android.masterdistributormdl.gskDistributor.view

import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.WebViewBinding
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.getAppFragmentManager
import com.android.masterdistributormdl.gskDistributor.utils.isNetworkAvailable
import com.android.masterdistributormdl.gskDistributor.utils.setOnBackResult
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToast
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.utils.SharedPreference

import com.gsk.distributor.model.ErrorAlert


class WebLoad : Fragment() {
    private lateinit var binding: WebViewBinding
    lateinit var model: WebModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.web_view, container, false)
        model = ViewModelProvider(this).get(WebModel::class.java)
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }


    private var downloadWeb: DownloadWeb? = null

    @Keep
    data class DownloadWeb(
        val url: String,
        val userAgent: String,
        val contentDisposition: String,
        val mimetype: String,
        val contentLength: Long
    )

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setHeader()
        }
    }


    private fun setHeader() {
        try {
            shooterFragment = this
            (activity as MainActivity).setHeader("WebView", STATUS_COLOR2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()

        val type = requireArguments().getString("type")
        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else if (it.status == 1) {
                InternetError.show(requireContext())
            }
        }
        //   titleView.text = requireArguments().getString("title")
        binding.titleView.text = "Please Wait..."
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            Loading.showHide2(requireContext(), it)
            binding.swipeRefreshLayout.isRefreshing = it
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            val url = binding.webView.url
            if (!url.isNullOrEmpty()) loadWebView(url)
        }
        binding.back.setOnClickListener { requireActivity().onBackPressed() }

        if (isNetworkAvailable(requireContext())) {
            if (type == "url") {
                val url = requireArguments().getString("url")!!
                loadWebView(url)
            }
        } else {
            AlertError.show(requireContext(), getString(R.string.internet_error)) {
                getAppFragmentManager(requireActivity()).popBackStack()
            }
        }


    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView(url: String) {
        val webSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowContentAccess = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.loadsImagesAutomatically = true
        webSettings.defaultTextEncodingName = "utf-8"
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = false
        webSettings.loadWithOverviewMode = true
        binding.webView.webViewClient = MyWebViewClient()
        binding.webView.webChromeClient = MyWebChromeClient()
        binding.webView.loadUrl(url)
        binding.webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            downloadWeb = DownloadWeb(
                url, userAgent, contentDisposition, mimeType, contentLength
            )
            downloadFile()
        }
    }

    inner class MyWebChromeClient : WebChromeClient() {
        override fun onGeolocationPermissionsShowPrompt(
            origin: String, callback: GeolocationPermissions.Callback
        ) {
        }

        override fun onProgressChanged(view: WebView?, progress: Int) {
            super.onProgressChanged(view, progress)
            if (binding.mainLayout != null) {
                binding.progressBar.progress = progress
            }
        }
    }


    fun setWebViewTitle(title: String?, url: String?) {
        if (!title.isNullOrEmpty())
            if (!title.startsWith("http"))
                binding.titleView.text = title

    }


    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent?.action) {
                showToast("Download Completed")
            }
        }
    }
    private var receiverRegistered = false
    override fun onDestroy() {
        super.onDestroy()
        if (receiverRegistered) requireActivity().unregisterReceiver(broadcastReceiver)

    }


    private var downloadManager: DownloadManager? = null


    inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            try {
                if (url.startsWith("http")) {
                    view.loadUrl(url)
                } else {
                    setOnBackResult(requireActivity(), "play_store", bundleOf("url" to url))
                    /*    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)*/

                }
            } catch (e: Exception) {
                //    showToast("Something went wrong")
            }
            return true
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (binding.mainLayout != null) {
                binding.webView.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                binding.swipeRefreshLayout.isRefreshing = true
                setWebViewTitle(view.title, view.url)
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            if (binding.mainLayout != null) {
                binding.webView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                setWebViewTitle(view.title, view.url)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun downloadFile() {
        val url = downloadWeb?.url
        val userAgent = downloadWeb?.url
        val contentDisposition = downloadWeb?.url
        val mimeType = downloadWeb?.url
        val contentLength = downloadWeb?.url
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        requireContext().registerReceiver(
            broadcastReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
        val cookies = CookieManager.getInstance().getCookie(url)
        receiverRegistered = true
        downloadManager =
            requireContext().getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setMimeType(mimeType);
        request.addRequestHeader("cookie", cookies);
        request.addRequestHeader("User-Agent", userAgent);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
        request.setDescription("Downloading File...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        downloadManager?.enqueue(request)
        showToast("Download Starting . . .")
    }

}

class WebModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val isDialogVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val user = sharedPreference.getUser()!!


}