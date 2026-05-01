package com.android.masterdistributormdl.profile


import android.graphics.BitmapFactory
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.addLead.AddLeadStatus
import com.android.masterdistributormdl.cropper.CropImageContract
import com.android.masterdistributormdl.cropper.CropOptions
import com.android.masterdistributormdl.databinding.EditProfileBinding
import com.android.masterdistributormdl.databinding.ProfileBinding

import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.model.profile.Data
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.addFragment
import com.android.masterdistributormdl.utils.checkMobileNo
import com.android.masterdistributormdl.utils.checkNullorEmpty
import com.android.masterdistributormdl.utils.clearAllEditTextFocus
import com.android.masterdistributormdl.utils.clearAllEditTextFocusError
import com.android.masterdistributormdl.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.utils.imageToBase64
import com.android.masterdistributormdl.utils.loadImageWithCoil
import com.android.masterdistributormdl.utils.replaceFragment
import com.android.masterdistributormdl.utils.scrollToPosition
import com.android.masterdistributormdl.utils.scrollToPosition1
import com.android.masterdistributormdl.utils.setEditText
import com.android.masterdistributormdl.utils.setError2
import com.android.masterdistributormdl.utils.setOnBackResult
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.gson.JsonObject
import java.io.File


class EditProfile : Fragment() {
    private lateinit var binding: EditProfileBinding
    lateinit var model: ProfileModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.edit_profile, container, false)
        model = ViewModelProvider(this)[ProfileModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        handleRetrofitMessage()
        getProfile()
        initListener()
        textWatcher()
        enableEditText()
    }

    private fun textWatcher() {
        binding.edtFullName.addTextChangedListener {
            if (checkNullorEmpty(binding.edtFullName)) {
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
            }
            else{
                binding.txtMobileError.visibility=View.GONE

            }
        }
    }

    private fun enableEditText() {
        setEditText(binding.edtFullName)
        setEditText(binding.mobileNumber)
    }

    private fun initListener() {
        binding.backButton.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }

        binding.txtChangeProfilePhoto.setOnClickListener {
            startCrop()
        }


        binding.btnSave.setOnClickListener {
            clearAllEditTextFocus(binding.edtFullName,binding.mobileNumber)
            clearAllEditTextFocusError(binding.txtClientError,binding.txtMobileError)

            if (checkNullorEmpty(binding.edtFullName)) {
                binding.txtClientError.visibility = View.VISIBLE
                scrollToPosition(binding.nested, binding.edtFullName)
            } else if (checkNullorEmpty(binding.mobileNumber)) {
                binding.txtMobileError.visibility = View.VISIBLE
                scrollToPosition(binding.nested, binding.mobileNumber)
            } else if (!checkMobileNo(binding.mobileNumber.text.toString())) {
                binding.txtMobileError.text = getString(R.string.please_enter_valid_mobile_number)
                binding.txtMobileError.visibility = View.VISIBLE
                scrollToPosition(binding.nested, binding.mobileNumber)
            } else if (checkNullorEmpty(binding.edtPincode)) {
                binding.txtPincodeError.visibility = View.VISIBLE
                scrollToPosition(binding.nested, binding.edtPincode)
            } else if (binding.edtPincode.text?.length!! < 6) {
                binding.txtPincodeError.text = getString(R.string.please_enter_valid_pincode)
                binding.txtPincodeError.visibility = View.VISIBLE
                scrollToPosition(binding.nested, binding.edtPincode)
            } else {
                editProfile()
            }
        }
    }

    private fun startCrop() {
        cropImage.launch(CropOptions {
            setAspectRatio(500, 500)
            setActivityTitle("Pick Image")
            setRequestedSize(300, 300)
            setAllowFlipping(true)
            setAllowRotation(true)
            setImageSource(includeGallery = true, includeCamera = true)
        })
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent!!
            val filePath = result.getUriFilePath(requireContext())
            val file = File(filePath)
            val bitmap = BitmapFactory.decodeFile(filePath)
            binding.image.setImageBitmap(bitmap)
            val base64 = imageToBase64(file)
            file.delete()
            model.editProfilePhoto(base64) {
                if (it.status == 0) {
                    showToastShort(it.message)
                    setOnBackResult(requireActivity(), "editProfile")
                } else {
                    AlertError.show(requireContext(), it.message) {}
                }

            }
        } else {
            val exception = result.error
            // AlertError.show(requireActivity(), exception!!.localizedMessage)
        }
    }

    private fun editProfile() {
        val clientName = binding.edtFullName.text.toString()
        var displayName = binding.displayName.text.toString()
        if (displayName.isNullOrEmpty()) {
            displayName = clientName
        }

        val mobileNumber = binding.mobileNumber.text.toString()
        var whatsAppNumber = binding.whatsappNumber.text.toString()
        if (whatsAppNumber.isNullOrEmpty()) {
            whatsAppNumber = mobileNumber
        }


        val param = JsonObject()
        param.addProperty("fullname", clientName)
        param.addProperty("mobile", mobileNumber)
        param.addProperty("whatsapp_mobile", whatsAppNumber)
        model.editProfile(param) {
            if (it.status == 0) {
                binding.displayName.setText(binding.edtFullName.text.toString())
                binding.whatsappNumber.setText(binding.mobileNumber.text.toString())
                setOnBackResult(requireActivity(), "editProfile")
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }
    }

    private fun getProfile() {
        val param = JsonObject()
        model.getProfile(param) {
            if (it.status == 0) {
                setData(it.data)
            }
        }
    }


    private fun setData(it: Data) {
        loadImageWithCoil(binding.image, it.profilephoto)
        binding.edtFullName.setText(it.fullname)
        binding.mobileNumber.setText(it.mobile)
        binding.whatsappNumber.setText(it.whatsapp)
        binding.emailAddress.setText(it.email)
        binding.edtCompanyName.setText(it.company)
        binding.edtPincode.setText(it.home_pincode)
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


