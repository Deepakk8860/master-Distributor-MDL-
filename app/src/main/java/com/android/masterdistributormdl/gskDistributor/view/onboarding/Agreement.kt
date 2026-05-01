package com.android.masterdistributormdl.gskDistributor.view.onboarding

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.AgreementBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.model.User
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.signature.SignaturePad
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.bitmapToBase64
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_data
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.utils.SharedPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Agreement : Fragment() {
    private lateinit var binding: AgreementBinding
    lateinit var model: AgreementModel
    var url = ""
    lateinit var from: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.agreement, container, false)
        model = ViewModelProvider(this)[AgreementModel::class.java]
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
            if (activity is MainActivity) (activity as MainActivity).setHeader(
                "Agreement",
                STATUS_COLOR2
            )
            else if (activity is OnboardActivity) (activity as OnboardActivity).setHeader(
                "Agreement",
                STATUS_COLOR2
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        from = requireArguments().getString("from")!!
        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else {
                InternetError.show(requireContext())
            }
        }
        model.errorMessage.observe(viewLifecycleOwner) {
            AlertError.show(requireContext(), it) {}
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireContext(), it)
        }
        binding.sign.isEnabled = false
        binding.bottomLay.visibility = View.GONE
        iniWebView()

        agreementText()

        fun scrollDown() {
            try {
                if (from != "onboard") return
                if (binding.scrollView.getChildAt(0).bottom <= binding.scrollView.height + binding.scrollView.scrollY) {
                    binding.sign.setBackgroundResource(R.drawable.login_button)
                    binding.sign.isEnabled = true
                } else {
                    binding.sign.setBackgroundResource(R.drawable.gray_button)
                    binding.sign.isEnabled = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            scrollDown()
        }
        binding.back.setOnClickListener { requireActivity().onBackPressed() }
        binding.sign.setOnClickListener {
            bottomSheetDialog()
        }
        binding.next.setOnClickListener {
            if (requireActivity() is OnboardActivity) {
                (requireActivity() as OnboardActivity).getUserProfile()
            }
        }
        binding.download.setOnClickListener {
            startDownloading(url)
        }
        binding.download1.setOnClickListener {
            startDownloading(url)
        }
        if (from == "menu") {
            binding.back.visibility = View.VISIBLE
            binding.downloadTopLay.visibility = View.VISIBLE
        } else {
            binding.back.visibility = View.GONE
            binding.downloadTopLay.visibility = View.GONE
        }
    }


    private fun startDownloading(url: String) {
        val user = model.getUser()
        val fileName = "Agreement_" + user.id + ".pdf"
        val downloader = DownloadFIle()
        downloader.download(requireContext(), url, null, fileName)
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun iniWebView() {
        binding.webView.setBackgroundColor(Color.TRANSPARENT)
        val webSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowContentAccess = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.loadsImagesAutomatically = true
        webSettings.defaultTextEncodingName = "utf-8"
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = false
        webSettings.loadWithOverviewMode = true
        webSettings.builtInZoomControls = true

        webSettings.displayZoomControls = false
        binding.webView.isHorizontalScrollBarEnabled = false

        binding.webView.overScrollMode = WebView.OVER_SCROLL_NEVER
        binding.webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    }

    private fun agreementText() {
        val param = JsonObject()

        model.agreementText(param) {
            if (it.get("status").asInt == 0) {

                url = it.get("download_agreement").asString
                setWebView("html", it.get("agreement").asString)
                if (url.isEmpty()) {
                    binding.downloadLay.visibility = View.GONE
                    binding.sign.visibility = View.VISIBLE
                    binding.signAlert.text = "Please tap the button “Sign” to sign the Agreement."
                } else {
                    if (from == "onboard") {
                        binding.downloadLay.visibility = View.VISIBLE
                        binding.sign.visibility = View.GONE
                        binding.signAlert.text = "You have signed the Agreement successfully."
                    }
                }
            } else {
                AlertError.show(requireContext(), it.get("message").asString) {}
            }
        }

    }

    private fun setWebView(type: String, value: String) {
        if (type == "html") {
            binding.webView.loadDataWithBaseURL(null, value, "text/html", "utf-8", null)
        } else {
            binding.webView.loadUrl(value)
        }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (binding.mainLayout == null) return
                if (from == "onboard") {
                    binding.bottomLay.visibility = View.VISIBLE
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun bottomSheetDialog() {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.agreement_sign)
        val bottomSheet = dialog.findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        val close = dialog.findViewById<ImageView>(R.id.close)!!
        val signature = dialog.findViewById<SignaturePad>(R.id.signatureView)!!
        val clear = dialog.findViewById<Button>(R.id.clear)!!
        val sign = dialog.findViewById<Button>(R.id.sign)!!
        dialog.show()
        clear.setOnClickListener {
            signature.clear()
        }
        close.setOnClickListener {
            dialog.dismiss()
        }
        sign.setOnClickListener {
            if (signature.isEmpty) {
                showToastShort("Submit your Signature to proceed further")
            } else {
                dialog.dismiss()
                val str = bitmapToBase64(signature.transparentSignatureBitmap)
                val param = JsonObject()
                param.addProperty("sign", str)
                model.upload_signature(param) {
                    if (it.status == 0) {
                        agreementText()
                    } else {
                        AlertError.show(requireContext(), it.message) {}
                    }
                }
            }
        }
    }
}

class AgreementModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    fun getUser(): User {
        return sharedPreference.getUserDist()!!
    }

    fun save(user: User) {
        sharedPreference.putString(user_data, gson.toJson(user))
    }

    fun agreementText(param: JsonObject, result: (JsonObject) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().agreementText(param).body()!!
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

    fun upload_signature(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().upload_signature(param).body()!!
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




