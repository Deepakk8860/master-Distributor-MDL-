package com.android.masterdistributormdl.gskDistributor.view.home
import android.app.Application
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.AddSalesBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject

import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.SuccessAlert
import com.android.masterdistributormdl.gskDistributor.utils.checkMobileNo
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.isEmailValid
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddSales : Fragment() {
    lateinit var model: AddSalesModel
    private lateinit var  binding: AddSalesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.add_sales, container, false)
        model = ViewModelProvider(this)[AddSalesModel::class.java]
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
        setHeader()
        binding.back.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()

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

        binding.save.setOnClickListener {
            validation()
        }
        binding.pincode.addTextChangedListener {
            if (it.toString().length == 6) {
                getPinLocation()
            } else {
                binding.district.setText("")
                binding.state.setText("")
            }
        }
        setClearError(binding.nameInputLay, binding.name)
        setClearError(binding.mobileInputLay, binding.mobile)
        setClearError(binding.emailInputLay, binding.email)
        setClearError(binding.addressInputLay, binding.address)
        setClearError(binding.pincodeInputLay, binding.pincode)
        //setClearError(cityInputLay, city)
        setClearError(binding.districtInputLay, binding.district)
        setClearError(binding.stateInputLay, binding.state)

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
        } else if (!isEmailValid(binding.email.text.toString())) {
            binding.emailInputLay.requestFocus()
            binding.emailInputLay.error = " "
        } else if (binding.address.text.toString().isEmpty()) {
            binding.addressInputLay.requestFocus()
            binding.addressInputLay.error = " "
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
            addSalesAgent()
        }

    }

    fun addSalesAgent() {
        val param = JsonObject()
        param.addProperty("name", binding.name.text.toString())
        param.addProperty("mobile", binding.mobile.text.toString())
        param.addProperty("email", binding.email.text.toString())
        param.addProperty("address", binding.address.text.toString())
        param.addProperty("binding.pincode", binding.pincode.text.toString())
        param.addProperty("district", binding.district.text.toString())
        param.addProperty("state", binding.state.text.toString())
        model.addSalesAgent(param) {
            if (it.status == 0) {
                SuccessAlert.show(requireContext(), it.message) {
                    binding.name.setText("")
                    binding.mobile.setText("")
                    binding.email.setText("")
                    binding.address.setText("")
                    binding.pincode.setText("")
                    binding.district.setText("")
                    binding.state.setText("")
                }
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }
    }


}


class AddSalesModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val user = sharedPreference.getUser()!!

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

    fun addSalesAgent(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().addSalesAgent(param).body()!!
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






