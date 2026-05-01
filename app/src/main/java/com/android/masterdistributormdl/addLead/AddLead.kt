package com.android.masterdistributormdl.addLead


import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.collection.emptyLongSet
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.AddClientBinding

import com.android.masterdistributormdl.home.HomeModel
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.checkMobileNo
import com.android.masterdistributormdl.utils.checkNullorEmpty
import com.android.masterdistributormdl.utils.clearAllEditTextFocus
import com.android.masterdistributormdl.utils.clearAllEditTextFocusError
import com.android.masterdistributormdl.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.utils.replaceFragment
import com.android.masterdistributormdl.utils.scrollToPosition
import com.android.masterdistributormdl.utils.scrollToPosition1
import com.android.masterdistributormdl.utils.setEditText
import com.android.masterdistributormdl.utils.setError2
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject


class AddLead : Fragment() {
    private lateinit var binding: AddClientBinding
    lateinit var model: HomeModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.add_client, container, false)
        model = ViewModelProvider(this)[HomeModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        handleRetrofitMessage()
        initListener()
        addTextChangeListener()
        setEditText(binding.clientName)
        setEditText(binding.mobileNumber)
        setEditText(binding.edtPincode)
    }


    private fun addTextChangeListener() {
        binding.clientName.addTextChangedListener {
            if (checkNullorEmpty(binding.clientName)) {
                binding.txtClientError.visibility=View.GONE
            }else{
                binding.txtClientError.visibility=View.GONE
            }
        }

        binding.mobileNumber.addTextChangedListener {
            if (checkNullorEmpty(binding.mobileNumber)) {
                binding.txtMobileError.visibility=View.GONE
            }
            else if (checkMobileNo(binding.mobileNumber.text.toString())) {
                binding.txtMobileError.visibility=View.GONE
                binding.mobileNumber.setError2()
            }
            else{
                binding.txtMobileError.visibility=View.GONE
                binding.mobileNumber.setBackgroundResource(R.drawable.edt_selected)

            }
        }

        binding.edtPincode.addTextChangedListener {
            if (checkNullorEmpty(binding.edtPincode)) {
                binding.txtPincodeError.visibility=View.GONE
            }
            if (binding.edtPincode.text?.length!! <6) {
                binding.txtPincodeError.visibility=View.GONE
            }
            else{
                binding.txtPincodeError.visibility=View.GONE
            }
        }
        binding.edtPincode.addTextChangedListener {
            if (it?.length==6){
                getPinLocation()
            }else{
                binding.llCityState.visibility=View.GONE
            }
        }
    }

    private fun initListener() {
        binding.backButton.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            addClientLead()
        }


        binding.displayName.addTextChangedListener {
            if (binding.displayName.text!!.isNotEmpty()){
                binding.copyIcon.visibility=View.GONE
            }else{
                binding.copyIcon.visibility=View.VISIBLE
            }
        }




        binding.copyIcon.setOnClickListener {
            val clientName=binding.clientName.text.toString()
            if (clientName.isNotEmpty()){
                binding.displayName.setText(clientName)
                binding.copyIcon.visibility=View.GONE
            }

        }
    }

    private fun addClientLead() {
        clearAllEditTextFocus(
            binding.clientName,
            binding.mobileNumber,
            binding.edtPincode,
        )

        clearAllEditTextFocusError(
            binding.txtClientError,
            binding.txtMobileError,
            binding.txtPincodeError,
        )

        if (checkNullorEmpty(binding.clientName)){
            binding.txtClientError.visibility=View.VISIBLE
            scrollToPosition(binding.nested,binding.clientName)
        }
        else if (checkNullorEmpty(binding.mobileNumber)){
            binding.txtMobileError.visibility=View.VISIBLE
            scrollToPosition(binding.nested,binding.mobileNumber)
        }
        else if (!checkMobileNo(binding.mobileNumber.text.toString())){
            binding.txtMobileError.text= getString(R.string.please_enter_valid_mobile_number)
            binding.txtMobileError.visibility=View.VISIBLE
            scrollToPosition(binding.nested,binding.mobileNumber)
        }
        else if (checkNullorEmpty(binding.edtPincode)){
            binding.txtPincodeError.visibility=View.VISIBLE
            scrollToPosition(binding.nested,binding.edtPincode)
        }
        else if (binding.edtPincode.text?.length!! <6){
            binding.txtPincodeError.text= getString(R.string.please_enter_valid_pincode)
            binding.txtPincodeError.visibility=View.VISIBLE
            scrollToPosition(binding.nested,binding.edtPincode)
        }
        else{
            addLead()
        }
    }


    private fun handleRetrofitMessage() {
        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else if (it.status == 1) {
                InternetError.show(requireContext())
            }
        }
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) AlertError.show(requireContext(), it) {}
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) { it ->
            Loading.showHide(requireActivity(), it)
        }
    }

    private fun getPinLocation() {
        val param = JsonObject()
        param.addProperty("pincode", binding.edtPincode.text.toString())
        model.getPinLocation(param) {
            if (it.get("status").asInt == 0) {
                val data = it.get("data").asJsonObject
                binding.edtCity.setText(data.get("districtname").asString)
                binding.edtState.setText(data.get("statename").asString)
                binding.llCityState.visibility=View.VISIBLE
            } else {
                binding.edtPincode.setText("")
                model.errorMessage.value = it.get("message").asString
            }
        }
    }

    private fun addLead() {
        val clientName= binding.clientName.text.toString()
        var displayName= binding.displayName.text.toString()
        if (displayName.isNullOrEmpty()){
            displayName=clientName
        }

        val mobileNumber= binding.mobileNumber.text.toString()
        var whatsAppNumber= binding.whatsappNumber.text.toString()
        if (whatsAppNumber.isNullOrEmpty()){
            whatsAppNumber=mobileNumber
        }

        val param = JsonObject()
        param.addProperty("client_name", clientName)
        param.addProperty("display_name", displayName)
        param.addProperty("mobile", mobileNumber)
        param.addProperty("whatsapp_num", whatsAppNumber)
        param.addProperty("email", binding.emailAddress.text.toString())
        param.addProperty("pincode", binding.edtPincode.text.toString())
        param.addProperty("city", binding.edtCity.text.toString())
        param.addProperty("state", binding.edtState.text.toString())
        param.addProperty("notes", binding.notes.text.toString())
        model.addLead(param) {
            if (it.status==0) {
                binding.displayName.setText(binding.clientName.text.toString())
                binding.whatsappNumber.setText(binding.mobileNumber.text.toString())
                showToastShort(it.message)
                replaceFragment(requireActivity(),AddLeadStatus(), bundleOf("leadId" to it.data.lead_id))
            } else {
                AlertError.show(requireContext(),it.message){}
            }
        }
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

}


