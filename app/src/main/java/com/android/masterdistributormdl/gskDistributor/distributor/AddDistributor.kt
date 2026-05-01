package com.android.masterdistributormdl.gskDistributor.distributor

import android.app.Application
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.adapter.DroupAdapter
import com.android.masterdistributormdl.adapter.PincodeAdapter
import com.android.masterdistributormdl.databinding.AddDistributorBinding
import com.android.masterdistributormdl.databinding.AddShopBinding
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.android.masterdistributormdl.gskDistributor.model.Territory
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
import com.android.masterdistributormdl.gskDistributor.utils.isEmailValid
import com.android.masterdistributormdl.gskDistributor.utils.setOnBackResult
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.gskDistributor.view.home.Home
import com.android.masterdistributormdl.gskDistributor.view.home.ShopCertificate
import com.android.masterdistributormdl.model.ApiResponse
import com.android.masterdistributormdl.model.LeadStageData
import com.android.masterdistributormdl.model.LeadStageResult
import com.android.masterdistributormdl.utils.SharedPreference
import com.android.masterdistributormdl.utils.checkNullorEmpty
import com.android.masterdistributormdl.utils.replaceFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.gsk.distributor.model.StateItem
import com.gsk.distributor.model.StateResult
import com.gsk.distributor.model.UserResult

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddDistributor : Fragment() {
    private lateinit var binding: AddDistributorBinding
    lateinit var model: AddDistributorModel
    val states = ArrayList<StateItem>()
    private var pincodeList = ArrayList<Territory>()
    private var planList = ArrayList<LeadStageData>()
    private var planId = ""
    private var leadId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.add_distributor, container, false)
        model = ViewModelProvider(this)[AddDistributorModel::class.java]
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

    @RequiresApi(Build.VERSION_CODES.O)
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
        statewisecode()
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

    private fun statewisecode() {
        model.statewisecode {
            states.clear()
            if (it.status == 0) {
                states.addAll(it.state)
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListener() {
        binding.txtOpportunity.setOnClickListener {
            openBottomSheetLead(
                4,
                planList as ArrayList<Any>,
                binding.txtOpportunity,
                "Select Plan"
            )
        }

        binding.etState.setOnClickListener {
            droupDialog(binding.etState, states as ArrayList<Any>)
        }

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

    private fun getPlanList() {
        model.getPlanList {
            if (it.status == 0) {
                planList = it.data
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openBottomSheetLead(
        viewType: Int,
        array: ArrayList<Any>,
        txtLeadStatus: AppCompatTextView,
        titleMain: String
    ) {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomSheet)
        dialog.setContentView(R.layout.bottom_sheet_title_icon)
        dialog.show()
        val close = dialog.findViewById<ImageView>(R.id.ivClose)
        val title = dialog.findViewById<TextView>(R.id.tvTitle)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        title?.text = titleMain

        val adapter = DroupAdapter(viewType)
        adapter.updateAdapter(array)
        recyclerView?.adapter = adapter
        recyclerView?.addItemDecoration(
            DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
        )

        adapter.setOnclickListener {
            dialog.dismiss()

            if (viewType == 4) {
                it as LeadStageData
                txtLeadStatus.text = it.name
                txtLeadStatus.tag = it.id
                planId = it.id
                getUserProfile()
            }
        }

        close?.setOnClickListener {
            dialog.dismiss()
        }


    }

    private fun getUserProfile() {
        model.getUserProfile {
            if (it.status == 0) {
                pincodeList = it.data.territory
                model.pincodeListAdapter.updateAdapter(pincodeList as ArrayList<Any>)
                binding.recPincodeList.adapter = model.pincodeListAdapter
            }
        }
    }

    private fun droupDialog(editText: EditText, array: ArrayList<Any>) {
        if (array.size == 0) {
            showToastShort("Please wait ...")
            return
        }
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.droup_alert)
        val sheet = dialog.findViewById<View?>(com.karumi.dexter.R.id.design_bottom_sheet)
        sheet?.setBackgroundColor(Color.TRANSPARENT)
        dialog.show()
        val close = dialog.findViewById<ImageView>(R.id.close)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = HomeAdapter(model, 19)
        adapter.updateAdapter(array)
        recyclerView?.adapter = adapter
        adapter.setOnclickListener {
            it as StateItem
            dialog.dismiss()
            editText.setText(it.gst_state)
        }

        close?.setOnClickListener {
            dialog.dismiss()
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
        }
        else if (checkNullorEmpty(binding.email)) {
            binding.emailInputLay.requestFocus()
            binding.emailInputLay.error = " "
        }
        else if (!isEmailValid(binding.email.toString())) {
            binding.emailInputLay.requestFocus()
            binding.emailInputLay.error = " "
        }
        else if (!checkMobileNo(binding.mobile.text.toString())) {
            binding.mobileInputLay.requestFocus()
            binding.mobileInputLay.error = " "
        }

        else if (binding.alternateEmail.text.toString().isNotEmpty() && !isEmailValid(binding.alternateEmail.text.toString())) {
            binding.emailInputLay.requestFocus()
            binding.emailInputLay.error = " "
        }

        else if (binding.alternateMobile.text.toString().isNotEmpty() && !checkMobileNo(binding.mobile.text.toString())) {
            binding.mobileInputLay.requestFocus()
            binding.mobileInputLay.error = " "
        }

        else if (binding.address.text.toString().isEmpty()) {
            binding.addressInputLay.requestFocus()
            binding.addressInputLay.error = " "
        }

        else if (binding.city.text.toString().isEmpty()) {
            binding.cityInputLay.requestFocus()
            binding.cityInputLay.error = " "
        }
        else if (binding.etState.text.toString().isEmpty()) {
            binding.stateLay.requestFocus()
            binding.stateLay.error = " "
        }
        else if (binding.pincode.text.toString().isEmpty()) {
            binding.pincodeInputLay.requestFocus()
            binding.pincodeInputLay.error = " "
        }
      else if (binding.pincode.text.toString().length != 6) {
            binding.pincodeInputLay.requestFocus()
            binding.pincodeInputLay.error = " "
        } else {
            addDistributor()
        }

    }

    private fun addDistributor() {
        val selectedListValue = JsonArray()
        for (item in model.pincodeSelectedList) {
            selectedListValue.add(item)
        }
        val param = JsonObject()
        param.addProperty("name", binding.name.text.toString())
        param.addProperty("email", binding.email.text.toString())
        param.addProperty("altemail", binding.alternateEmail.text.toString())
        param.addProperty("mobile", binding.mobile.text.toString())
        param.addProperty("altmobile", binding.alternateMobile.text.toString())
        param.addProperty("address", binding.address.text.toString())
        param.addProperty("city", binding.city.text.toString())
        param.addProperty("state", binding.state.text.toString())
        param.addProperty("pincode", binding.pincode.text.toString())
        param.addProperty("ter_pin", binding.pincode.text.toString())
        param.addProperty("planname", planId)
        param.add("ter_pin", selectedListValue)

        model.addDistributor(param) {
            if (it.status == 0) {
                replaceFragment(requireActivity(),Home())
//                param.addProperty("shopid", it.shopid)
//                SuccessAlert.show(requireContext(), it.message) {
//                    onBackResult("ShopCertificate")
//                    addFragment(
//                        requireActivity(), ShopCertificate(), bundleOf("data" to param.toString())
//                    )
//
//                }
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


class AddDistributorModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val isDialogVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val retrofitError2 = MutableLiveData<com.android.masterdistributormdl.model.ErrorAlert>()
    var user = sharedPreference.getUserDist()!!
    var shopQuota: ShopQuota? = null
    val pincodeListAdapter =
        com.android.masterdistributormdl.gskDistributor.adapter.PincodeAdapter(this, 1)
    var pincodeSelectedList=ArrayList<String>()

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

    fun getUserProfile(result: (UserResult) -> Unit) {
        isLoaderVisible.value=true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getUserProfile(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value=false
                result.invoke(it)
            }.onFailure {
                isLoaderVisible.value=false
                retrofitError.postValue(
                    errorRetrofit(
                        it
                    )
                )
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

    fun addDistributor(param: JsonObject, result: (com.gsk.distributor.model.ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().addDistributor(param).body()!!
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
    fun getPlanList(result: (LeadStageResult) -> Unit) {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(com.android.masterdistributormdl.utils.user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    com.android.masterdistributormdl.network.getClient().getPlanList(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                result(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError2.postValue(com.android.masterdistributormdl.network.errorRetrofit(it))

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

    fun statewisecode(result: (StateResult) -> Unit) {

        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().statewisecode(param).body()!!
                }
            }.onSuccess {

                result.invoke(it)
            }.onFailure {

                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

}






