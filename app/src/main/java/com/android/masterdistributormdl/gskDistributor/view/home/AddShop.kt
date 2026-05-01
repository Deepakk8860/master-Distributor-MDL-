package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.AddShopBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.gsk.distributor.model.AddShopResult
import com.gsk.distributor.model.ErrorAlert
import com.gsk.distributor.model.ShopQuota
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.JsonObj
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR1
import com.android.masterdistributormdl.gskDistributor.utils.SuccessAlert
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.checkGstinNo
import com.android.masterdistributormdl.gskDistributor.utils.checkMobileNo
import com.android.masterdistributormdl.gskDistributor.utils.getAppFragmentManager
import com.android.masterdistributormdl.gskDistributor.utils.getHtmlSpanned
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.setOnBackResult
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.model.ApiResponse
import com.android.masterdistributormdl.utils.SharedPreference

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddShop : Fragment() {
    private lateinit var binding: AddShopBinding
    lateinit var model: AddShopModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.add_shop, container, false)
        model = ViewModelProvider(this)[AddShopModel::class.java]
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        binding.back.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()

        }
        initView()
        initListener()
        addTextWatcher()
        setClearError(binding.nameInputLay, binding.name)
        setClearError(binding.mobileInputLay, binding.mobile)
        setClearError(binding.shopInputLay, binding.shop)
        setClearError(binding.gstInputLay, binding.gstNo)
        setClearError(binding.pincodeInputLay, binding.pincode)
        setClearError(binding.addressInputLay, binding.address)
        setClearError(binding.landmarkInputLay, binding.landmark)
        setClearError(binding.cityInputLay, binding.city)
        setClearError(binding.landmarkInputLay, binding.landmark)
        setClearError(binding.districtInputLay, binding.district)
        setClearError(binding.stateInputLay, binding.state)

    }

    private fun addTextWatcher() {
        binding.mobile.addTextChangedListener {
            if (binding.mobile.text.toString().length==10){
                verifyMobile()
            }
        }
    }

    private fun verifyMobile() {
        val param=JsonObject()
        param.addProperty("mobile",binding.mobile.text.toString())
        param.addProperty("type","SHOP")
        model.verifyMobile(param){
            if (it.status==0){
//                showToastShort(it.message)
            }else{
                binding.mobile.setText("")
                AlertError.show(requireContext(),it.message){}
            }
        }
    }

    private fun initListener() {
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
            Loading.showHide(requireActivity(), it)
        }
        binding.pincode.addTextChangedListener {
            if (it.toString().length == 6) {
                getPinLocation()
            } else {
                binding.district.setText("")
                binding.state.setText("")
            }
        }
        binding.save.setOnClickListener {
            validation()
        }

        binding.btnSelfRetailer.setOnClickListener {
            setSelfRetailDetails()
        }
    }

    private fun setSelfRetailDetails() {
        binding.name.setText(model.user.fullname)
        binding.mobile.setText(model.user.mobile)
        binding.gstNo.setText(model.user.gstin)
        binding.address.setText(model.user.home_address)
        binding.city.setText(model.user.home_city)
        binding.pincode.setText(model.user.home_pincode)

        if (model.user.home_pincode.length == 6) {
            getPinLocation()
        } else {
            binding.district.setText("")
            binding.state.setText("")
        }
    }

    private fun initView() {
        if (model.user.isApplyRetailer){
            binding.btnSelfRetailer.visibility=View.GONE
        }else{
            binding.btnSelfRetailer.visibility=View.GONE
//            binding.btnSelfRetailer.visibility=View.VISIBLE
        }
        val available = model.shopQuota?.available
        binding.availShop.text =
            getHtmlSpanned("Available Retailer <font color='#1D3667'><b>$available</b></font>")
        if (requireArguments().getBoolean("unlimited")) {
            binding.availShop.visibility = View.GONE
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

    private fun validation() {
        if (binding.name.text.toString().isEmpty()) {
            binding.nameInputLay.requestFocus()
            binding.nameInputLay.error = " "
        } else if (!checkMobileNo(binding.mobile.text.toString())) {
            binding.mobileInputLay.requestFocus()
            binding.mobileInputLay.error = " "
        } else if (binding.shop.text.toString().isEmpty()) {
            binding.shopInputLay.requestFocus()
            binding.shopInputLay.error = " "
        } else if (binding.gstNo.text.toString().isNotEmpty() && !checkGstinNo(binding.gstNo.text.toString())) {
            binding.gstInputLay.requestFocus()
            binding.gstInputLay.error = " "
            showToastShort("Invalid GSTIN Number")
        } else if (binding.address.text.toString().isEmpty()) {
            binding.addressInputLay.requestFocus()
            binding.addressInputLay.error = " "
        } else if (binding.city.text.toString().isEmpty()) {
            binding.cityInputLay.requestFocus()
            binding.cityInputLay.error = " "
        } else if (binding.pincode.text.toString().length != 6) {
            binding.pincodeInputLay.requestFocus()
            binding.pincodeInputLay.error = " "
        } else if (binding.district.text.toString().isEmpty()) {
            binding.districtInputLay.requestFocus()
            binding.districtInputLay.error = " "
        } else if (binding.state.text.toString().isEmpty()) {
            binding.stateInputLay.requestFocus()
            binding.stateInputLay.error = " "
        } else {
            addShop()
        }

    }

    fun addShop() {
        val param = JsonObject()
        param.addProperty("contact_name", binding.name.text.toString())
        param.addProperty("contact_no", binding.mobile.text.toString())
        param.addProperty("gstin", binding.gstNo.text.toString())
        param.addProperty("shop", binding.shop.text.toString())
        param.addProperty("pincode", binding.pincode.text.toString())
        param.addProperty("address", binding.address.text.toString())
        param.addProperty("landmark", binding.landmark.text.toString())
        param.addProperty("city", binding.city.text.toString())
        param.addProperty("district", binding.district.text.toString())
        param.addProperty("state", binding.state.text.toString())

        model.addShop(param) {
            if (it.status == 0) {
                param.addProperty("shopid", it.shopid)
                SuccessAlert.show(requireContext(), it.message) {
                    onBackResult("ShopCertificate")
                    addFragment(
                        requireActivity(), ShopCertificate(), bundleOf("data" to param.toString())
                    )

                }
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }
    }

    fun onBackResult(result_key: String) {
        getAppFragmentManager(requireActivity()).setFragmentResultListener(
            result_key, viewLifecycleOwner
        ) { requestKey, bundle ->
            setOnBackResult(requireActivity(), "ShopCount")
        }
    }

    private fun getPinLocation() {
        model.getPinLocation(binding.pincode.text.toString()) {
            if (it.get("status").asInt == 0) {
                val data = it.get("data").asJsonObject
                binding.district.setText(data.get("districtname").asString)
                binding.state.setText(data.get("statename").asString)
            } else {
                binding.pincode.setText("")
                model.errorMessage.value = it.get("message").asString
            }
        }
    }

}


class AddShopModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val isDialogVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUserDist()!!
    var shopQuota: ShopQuota? = null

    init {
        val json = sharedPreference.getString("shopquota")
        if (json!!.isNotEmpty()) {
            shopQuota = gson.fromJson(json, object : TypeToken<ShopQuota>() {}.type)
        }

    }


    fun getPinLocation(pincode: String, result: (JsonObject) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("pincode", pincode)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getPinLocation(param).body()!!
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


    fun addShop(param: JsonObject, result: (AddShopResult) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().addShop(param).body()!!
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

    fun verifyMobile(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().verifyMobile(param).body()!!
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

    fun shopCerticate(shopid: String, url: (String?) -> Unit) {
        isDialogVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("shopid", shopid)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().generateshopCerticate(param).body()!!
                }
            }.onSuccess {
                isDialogVisible.value = false
                val it = JsonObj(it)
                if (it.getInt("status") == 0) {
                    url.invoke(it.getString("uri"))
                } else {
                    showToastShort(it.getString("message"))
                }
            }.onFailure {
                isDialogVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

}






