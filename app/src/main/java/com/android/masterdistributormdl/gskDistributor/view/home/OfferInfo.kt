package com.android.masterdistributormdl.gskDistributor.view.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import com.android.masterdistributormdl.utils.SharedPreference
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.OfferInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.ErrorAlert
import com.gsk.distributor.model.OfferItem
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.OFFERS
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.SuccessAlert
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.checkMobileNo
import com.android.masterdistributormdl.gskDistributor.utils.getAppFragmentManager
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.isEmailValid
import com.android.masterdistributormdl.gskDistributor.utils.loadImage
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.gskDistributor.view.WebLoad
import com.gsk.distributor.model.TrainingStatusResult

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class OfferInfo : Fragment() {
    lateinit var model: OffersModel
    lateinit var item: OfferItem
    private lateinit var binding: OfferInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.offer_info, container, false)
        model = ViewModelProvider(this)[OffersModel::class.java]
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        item = requireArguments().getSerializable("data") as OfferItem
        setHeader()

        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else {
                InternetError.show(requireContext())
            }
        }
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            Loading.showHide2(requireContext(), it)
        }
        model.isDialogVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireContext(), it)
        }
        binding.applyNow.setOnClickListener {
            if (item.url.isEmpty()) {
                applyNowDialog()
            } else {
                val user = model.user
                offerEnquiry(user.fullname, user.mobile, user.email)

            }
        }
        binding.back.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }
        binding.bottomlLay.visibility = View.GONE

        loadImage(binding.image, item.banner)
        binding.title.text = item.title
        iniWebView()

    }
    private fun openWebView() {
        val bundle = bundleOf("type" to "url", "url" to item.url, "title" to item.title)
        addFragment(requireActivity(), WebLoad(), bundle)
        onBackResult("play_store")
    }

    private fun onBackResult(result_key: String) {
        getAppFragmentManager(requireActivity()).setFragmentResultListener(
            result_key, this
        ) { requestKey, bundle ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(bundle.getString("url")))
            startActivity(intent)
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    fun iniWebView() {
        binding.webView.setBackgroundColor(Color.TRANSPARENT)
        val webSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowContentAccess = true
        webSettings.loadsImagesAutomatically = true
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = false
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.loadWithOverviewMode = true
        binding.webView.scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        binding.webView.loadDataWithBaseURL(null, item.description, "text/html", "utf-8", null)
        binding.webView.webViewClient = MyWebViewClient()
    }

    fun next(id: String) {
        model.offerimage2(id) {
            val bundle = bundleOf("data" to it)
            addFragment(requireActivity(), OfferInfo(), bundle)
        }
    }

    inner class MyWebViewClient : WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            try {
                Log.d(TAG, "WebViewUrl : " + url)
                if (url.contains("https://offer")) {
                    next(url)
                } else {

                }
            } catch (e: Exception) {

            }
            return true
        }

        override fun onReceivedError(
            view: WebView?, request: WebResourceRequest?, error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)

        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (binding.mainLayout != null) {

            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            if (binding.mainLayout != null)
                binding.bottomlLay.visibility = View.VISIBLE
        }
    }


    private fun applyNowDialog() {
        val user = model.user
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.offer_apply)
        val bottomSheet = dialog.findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)

        val save = dialog.findViewById<Button>(R.id.save)!!
        val close = dialog.findViewById<ImageView>(R.id.close)!!
        val nameInputLay = dialog.findViewById<TextInputLayout>(R.id.nameInputLay)!!
        val emailInputLay = dialog.findViewById<TextInputLayout>(R.id.emailInputLay)!!
        val mobileInputLay = dialog.findViewById<TextInputLayout>(R.id.mobileInputLay)!!
        val name = dialog.findViewById<TextInputEditText>(R.id.name)!!
        val mobile = dialog.findViewById<TextInputEditText>(R.id.mobile)!!
        val email = dialog.findViewById<TextInputEditText>(R.id.email)!!
        dialog.show()
        setClearError(nameInputLay, name)
        setClearError(mobileInputLay, mobile)
        setClearError(emailInputLay, email)
        name.setText(user.fullname)
        mobile.setText(user.mobile)
        email.setText(user.email)

        close.setOnClickListener {
            dialog.dismiss()
        }
        save.setOnClickListener {
            if (name.text.toString().isEmpty()) {
                nameInputLay.requestFocus()
                nameInputLay.error = " "
            } else if (!checkMobileNo(mobile.text.toString())) {
                mobileInputLay.requestFocus()
                mobileInputLay.error = " "
            } else if (email.text.toString().isNotEmpty() && !isEmailValid(email.text.toString())) {
                emailInputLay.requestFocus()
                emailInputLay.error = " "
            } else {
                dialog.dismiss()
                offerEnquiry(name.text.toString(), mobile.text.toString(), email.text.toString())
            }
        }

    }

    private fun setClearError(textInputLayout: TextInputLayout, editText: TextInputEditText) {
        editText.addTextChangedListener {
            textInputLayout.error = null
            if (it.isNullOrEmpty()) {
                editText.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._12sp)
                )
            } else {
                editText.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._14sp)
                )
            }
        }
    }
    private fun offerEnquiry(name: String, mobile: String, email: String) {
        val param = JsonObject()
        param.addProperty("name", name)
        param.addProperty("mobile", mobile)
        param.addProperty("email", email)
        param.addProperty("offer_id", item.id)
        model.offerEnquiry(param) {
            if (it.status == 0) {
                if (item.url.isEmpty()) {
                    SuccessAlert.show(requireActivity(), it.message) {
                        requireActivity().onBackPressed()
                    }
                } else {
                    openWebView()
                }
            } else {
                model.errorMessage.value = it.message
            }
        }
    }

}

class OffersModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val isDialogVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val user = sharedPreference.getUser()!!

    val offerAdapter = HomeAdapter(this, 17)

    init {
        val json = sharedPreference.getString(OFFERS)
        if (json!!.isNotEmpty()) {
            val it: ArrayList<OfferItem> =
                gson.fromJson(json, object : TypeToken<ArrayList<OfferItem>>() {}.type)
            offerAdapter.updateAdapter(it as ArrayList<Any>)
        }
    }

    fun offerimage() {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))

        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().offerimage(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                var array = ArrayList<OfferItem>()
                if (it.status == 0) {
                    array = it.data
                } else {
                    errorMessage.value = it.message
                }
                sharedPreference.putString(OFFERS, gson.toJson(array))
                offerAdapter.updateAdapter(array as ArrayList<Any>)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun offerEnquiry(param: JsonObject, result: (ApiResponse) -> Unit) {

        param.addProperty("uid", sharedPreference.getString(user_id))
        isDialogVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().offer_enquiry(param).body()!!
                }
            }.onSuccess {
                isDialogVisible.value = false
                result.invoke(it)
            }.onFailure {
                isDialogVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun getTrainingStatus(result: (TrainingStatusResult) -> Unit) {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        isDialogVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getTrainingStatus(param).body()!!
                }
            }.onSuccess {
                isDialogVisible.value = false
                result.invoke(it)
            }.onFailure {
                isDialogVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun offerimage2(offer_id: String, result: (OfferItem) -> Unit) {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("offer_id", offer_id)
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().offerimage(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false

                if (it.status == 0) {
                    if (it.data.size > 0)
                        result.invoke(it.data[0])
                } else {
                    errorMessage.value = it.message
                }

            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }


}



