package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.FragmentAddEditAgentBinding
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.utils.SharedPreference
import com.google.gson.JsonObject
import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.ErrorAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddEditAgentScreen : Fragment() {
    private lateinit var binding: FragmentAddEditAgentBinding
    private lateinit var model: AddEditAgentModel
    private var agentId: String? = null
    private var hasAttemptedSubmit = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_edit_agent, container, false)
        model = ViewModelProvider(this)[AddEditAgentModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
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
            requireActivity().onBackPressed()
        }

        // Get arguments if present (for Edit mode)
        arguments?.let {
            agentId = it.getString("agent_id")
            binding.name.setText(it.getString("name"))
            binding.email.setText(it.getString("email"))
            binding.mobile.setText(it.getString("mobile"))
            binding.pincode.setText(it.getString("pincode"))
            binding.address.setText(it.getString("address"))

            // Trigger validations without displaying errors immediately to set correct inputs green
            validateName(false)
            validateEmail(false)
            validateMobile(false)
            validatePincode(false)
            validateAddress(false)
        }

        // Handle Add/Edit texts conditionally
        if (agentId.isNullOrEmpty()) {
            binding.txtHeaderTitle.text = "Add Agent"
            binding.btnSave.text = "Add Agent"
        } else {
            binding.txtHeaderTitle.text = "Edit Agent"
            binding.btnSave.text = "Edit Agent"
        }

        setupValidationListeners()

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

        model.isDialogVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireActivity(), it)
        }

        binding.btnSave.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun setupValidationListeners() {
        binding.name.addTextChangedListener {
            validateName(hasAttemptedSubmit)
        }
        binding.email.addTextChangedListener {
            validateEmail(hasAttemptedSubmit)
        }
        binding.mobile.addTextChangedListener {
            validateMobile(hasAttemptedSubmit)
        }
        binding.pincode.addTextChangedListener {
            validatePincode(hasAttemptedSubmit)
        }
        binding.address.addTextChangedListener {
            validateAddress(hasAttemptedSubmit)
        }
    }

    private fun setFieldSuccess(layout: com.google.android.material.textfield.TextInputLayout) {
        layout.error = null
        val states = arrayOf(
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(-android.R.attr.state_focused)
        )
        val colors = intArrayOf(
            Color.parseColor("#34A853"),
            Color.parseColor("#34A853")
        )
        layout.setBoxStrokeColorStateList(ColorStateList(states, colors))
    }

    private fun setFieldError(layout: com.google.android.material.textfield.TextInputLayout, errorMsg: String) {
        layout.error = errorMsg
    }

    private fun clearFieldOutline(layout: com.google.android.material.textfield.TextInputLayout) {
        layout.error = null
        val states = arrayOf(
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(-android.R.attr.state_focused)
        )
        val colors = intArrayOf(
            Color.parseColor("#132D5F"),
            Color.parseColor("#CCD2E3")
        )
        layout.setBoxStrokeColorStateList(ColorStateList(states, colors))
    }

    private fun validateName(showError: Boolean): Boolean {
        val name = binding.name.text.toString().trim()
        return if (name.isEmpty()) {
            if (showError) setFieldError(binding.nameInputLay, "Please enter name")
            else clearFieldOutline(binding.nameInputLay)
            false
        } else {
            setFieldSuccess(binding.nameInputLay)
            true
        }
    }

    private fun validateEmail(showError: Boolean): Boolean {
        val email = binding.email.text.toString().trim()
        return if (email.isEmpty()) {
            if (showError) setFieldError(binding.emailInputLay, "Please enter email")
            else clearFieldOutline(binding.emailInputLay)
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (showError) setFieldError(binding.emailInputLay, "Please enter a valid email address")
            else clearFieldOutline(binding.emailInputLay)
            false
        } else {
            setFieldSuccess(binding.emailInputLay)
            true
        }
    }

    private fun validateMobile(showError: Boolean): Boolean {
        val mobile = binding.mobile.text.toString().trim()
        return if (mobile.isEmpty()) {
            if (showError) setFieldError(binding.mobileInputLay, "Please enter mobile number")
            else clearFieldOutline(binding.mobileInputLay)
            false
        } else if (mobile.length != 10) {
            if (showError) setFieldError(binding.mobileInputLay, "Mobile number must be 10 digits")
            else clearFieldOutline(binding.mobileInputLay)
            false
        } else {
            setFieldSuccess(binding.mobileInputLay)
            true
        }
    }

    private fun validatePincode(showError: Boolean): Boolean {
        val pincode = binding.pincode.text.toString().trim()
        return if (pincode.isEmpty()) {
            if (showError) setFieldError(binding.pincodeInputLay, "Please enter pincode")
            else clearFieldOutline(binding.pincodeInputLay)
            false
        } else if (pincode.length != 6) {
            if (showError) setFieldError(binding.pincodeInputLay, "Pincode must be 6 digits")
            else clearFieldOutline(binding.pincodeInputLay)
            false
        } else {
            setFieldSuccess(binding.pincodeInputLay)
            true
        }
    }

    private fun validateAddress(showError: Boolean): Boolean {
        val address = binding.address.text.toString().trim()
        return if (address.isEmpty()) {
            if (showError) setFieldError(binding.addressInputLay, "Please enter address")
            else clearFieldOutline(binding.addressInputLay)
            false
        } else {
            setFieldSuccess(binding.addressInputLay)
            true
        }
    }

    private fun validateAndSubmit() {
        hasAttemptedSubmit = true

        // Clear all errors first, then show only the first failing field error
        clearFieldOutline(binding.nameInputLay)
        clearFieldOutline(binding.emailInputLay)
        clearFieldOutline(binding.mobileInputLay)
        clearFieldOutline(binding.pincodeInputLay)
        clearFieldOutline(binding.addressInputLay)

        if (!validateName(true)) return
        if (!validateEmail(true)) return
        if (!validateMobile(true)) return
        if (!validatePincode(true)) return
        if (!validateAddress(true)) return

        val name = binding.name.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val mobile = binding.mobile.text.toString().trim()
        val pincode = binding.pincode.text.toString().trim()
        val address = binding.address.text.toString().trim()

        val param = JsonObject().apply {
            addProperty("name", name)
            addProperty("email", email)
            addProperty("mobile", mobile)
            addProperty("pincode", pincode)
            addProperty("address", address)
        }

        if (agentId.isNullOrEmpty()) {
            model.addAgent(param) { response ->
                showToastShort(response.message)
                if (response.status == 0) {
                    requireActivity().onBackPressed()
                }
            }
        } else {
            param.addProperty("edit_id", agentId)
            model.editAgent(param) { response ->
                showToastShort(response.message)
                if (response.status == 0) {
                    requireActivity().onBackPressed()
                }
            }
        }
    }
}

class AddEditAgentModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isDialogVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()

    fun addAgent(param: JsonObject, result: (ApiResponse) -> Unit) {
        isDialogVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().addAgent(param).body()!!
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

    fun editAgent(param: JsonObject, result: (ApiResponse) -> Unit) {
        isDialogVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().editAgent(param).body()!!
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
}
