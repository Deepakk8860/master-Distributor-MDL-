package com.android.masterdistributormdl.gskDistributor.view.home


import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.android.masterdistributormdl.utils.SharedPreference
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ViewCertificate2Binding
import com.google.gson.JsonObject
import com.gsk.distributor.model.CertificateUrl
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToast
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ViewCertificate2 : Fragment() {
    private lateinit var binding: ViewCertificate2Binding
    lateinit var model: CertificateModel2
    var url = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.view_certificate2, container, false)
        model = ViewModelProvider(this)[CertificateModel2::class.java]
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
            (activity as MainActivity).setHeader("", STATUS_COLOR2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()

        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else {
                InternetError.show(requireContext())
            }
        }
        binding.back.setOnClickListener { requireActivity().onBackPressed() }
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            Loading.showHide2(requireContext(), it)
            binding.swipeRefreshLayout.isRefreshing = it
        }
        binding.swipeRefreshLayout.isEnabled=false
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadWebview()
        }
        //  generateshopCerticate()
        loadWebview()
        binding.share.setOnClickListener {

        }


    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadWebview() {
        val webSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowContentAccess = true
        webSettings.allowFileAccess  = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.loadsImagesAutomatically = true
        webSettings.defaultTextEncodingName = "utf-8"
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = false
        webSettings.loadWithOverviewMode = true
       webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls=true
        webSettings.blockNetworkImage = true
        webSettings.displayZoomControls = false
        webSettings.setNeedInitialFocus(false)
        binding.webView.webViewClient = MyWebViewClient()
        binding.webView.webChromeClient = WebChromeClient()
        val url = "file:///android_asset/index.html?https://eshoapping.com/sample.pdf"
        binding.webView.loadUrl(url)
    }

    fun generateshopCerticate() {
        model.generateshopCerticate {

        }
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
            showToast("Got Error! ${error?.description.toString()}")

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

class CertificateModel2(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val user = sharedPreference.getUser()!!

    fun generateshopCerticate(result: (CertificateUrl) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
   viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().generateMFCerticate(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                result.invoke(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }

        }
  }
}




