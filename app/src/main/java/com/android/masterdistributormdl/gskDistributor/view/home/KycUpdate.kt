package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import com.android.masterdistributormdl.utils.SharedPreference
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.cropper.CropImageContract
import com.android.masterdistributormdl.cropper.CropOptions
import com.android.masterdistributormdl.databinding.KycUpdateBinding
import com.android.masterdistributormdl.databinding.VerifyAadharOtpBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.model.IfscResult
import com.gsk.distributor.model.StateItem
import com.gsk.distributor.model.StateResult
import com.android.masterdistributormdl.gskDistributor.model.User
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.AddTextWatcher
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.SuccessAlert
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.bitmapToFile
import com.android.masterdistributormdl.gskDistributor.utils.checkAadharNo
import com.android.masterdistributormdl.gskDistributor.utils.checkPANNo
import com.android.masterdistributormdl.gskDistributor.utils.checkPincode
import com.android.masterdistributormdl.gskDistributor.utils.getAadhaarNoFromText
import com.android.masterdistributormdl.gskDistributor.utils.getDateFormat
import com.android.masterdistributormdl.gskDistributor.utils.getPanNoFromText
import com.android.masterdistributormdl.gskDistributor.utils.getTextImage
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.imageToBase64
import com.android.masterdistributormdl.gskDistributor.utils.latitude
import com.android.masterdistributormdl.gskDistributor.utils.loadImage
import com.android.masterdistributormdl.gskDistributor.utils.longitude
import com.android.masterdistributormdl.gskDistributor.utils.printDataFormat
import com.android.masterdistributormdl.gskDistributor.utils.setClearError
import com.android.masterdistributormdl.gskDistributor.utils.setOnBackResult
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.gskDistributor.view.VideoKyc
import com.android.masterdistributormdl.utils.getHtmlSpanned
import com.android.masterdistributormdl.utils.sharedPreference
import com.gsk.distributor.model.ApiResponseAadhar
import kotlinx.coroutines.CoroutineScope


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit


class KycUpdate : Fragment() {
    lateinit var model: KycModel
    private lateinit var binding: KycUpdateBinding
    var gender = ""
    val states = ArrayList<StateItem>()
    private var _otpBinding: VerifyAadharOtpBinding? = null
    private val otpBinding get() = _otpBinding!!
    lateinit var imageView: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.kyc_update, container, false)
        model = ViewModelProvider(this)[KycModel::class.java]
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
            (requireActivity() as MainActivity).setHeader("", STATUS_COLOR2)
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
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireActivity(), it)
        }
        if (sharedPreference.getBoolean("isAadhar")){
            binding.btnAadharVerify.visibility=View.GONE

        }else{
            binding.btnAadharVerify.visibility=View.VISIBLE
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
        binding.selectAadharFrontImage.setOnClickListener { startCrop(binding.aadharFrontImage) }
        binding.selectAadharBackImage.setOnClickListener { startCrop(binding.aadharBackImage) }
        binding.selectPanImage.setOnClickListener { startCrop(binding.panImage) }
        binding.etAadharNo.addTextChangedListener(AddTextWatcher())
        binding.panViewLay.visibility = View.GONE
        binding.aadharViewLay.visibility = View.GONE

        binding.panImageLay.visibility = View.GONE
        binding.aadharFrontImageLay.visibility = View.GONE
        binding.aadharBackImageLay.visibility = View.GONE

        val user = model.user
        if (user.video) {
            binding.videoKycButton.visibility = View.GONE
        } else {
            binding.videoKycButton.visibility = View.VISIBLE
        }

        if (user.kyc_status!!.aadhaar_status){
            sharedPreference.putBoolean("isAadhar",true)
            binding.btnAadharVerify.visibility=View.GONE
        }
        binding.genderRG.setOnCheckedChangeListener { radioGroup, i ->
            if (binding.maleRB.isChecked) {
                gender = "Male"
                binding.maleRB.buttonTintList =
                    (ColorStateList.valueOf(Color.parseColor("#F86202")))
                binding.femaleRB.buttonTintList =
                    ColorStateList.valueOf(Color.parseColor("#1D3667"))
            } else {
                gender = "Female"
                binding.maleRB.buttonTintList = ColorStateList.valueOf(Color.parseColor("#1D3667"))
                binding.femaleRB.buttonTintList =
                    ColorStateList.valueOf(Color.parseColor("#F86202"))
            }
        }

        binding.videoKycButton.setOnClickListener {
            addFragment(requireActivity(), VideoKyc())
        }
        binding.btnAadharVerify.setOnClickListener {
            val aadhar = binding.etAadharNo.text.toString().replace(" ", "")
            if (!checkAadharNo(aadhar)) {
                binding.aadharNoLay.requestFocus()
                binding.aadharNoLay.error = " "
            } else {
                sendAadharOtp(aadhar)
            }
        }

        binding.relDistributorApp.setOnClickListener {
            addFragment(requireActivity(), ConsultantDetails())
        }
        binding.panCardButton.setOnClickListener {
            if (binding.panViewLay.visibility == View.VISIBLE) {
                binding.panViewLay.visibility = View.GONE
                binding.panIndicator.rotation = 0f
            } else {
                binding.panViewLay.visibility = View.VISIBLE
                binding.panIndicator.rotation = 180f
            }
        }
        binding.etDob.setOnClickListener { datePicker() }
        binding.aadharCardButton.setOnClickListener {
            if (binding.aadharViewLay.visibility == View.VISIBLE) {
                binding.aadharViewLay.visibility = View.GONE
                binding.aadharIndicator.rotation = 0f
            } else {
                binding.aadharViewLay.visibility = View.VISIBLE
                binding.aadharIndicator.rotation = 180f
            }
        }
        binding.aadharFrontClose.setOnClickListener {
            binding.aadharFrontImageLay.visibility = View.GONE
            binding.selectAadharFrontImage.visibility = View.VISIBLE
            binding.aadharFrontImage.tag = null
        }
        binding.aadharBackClose.setOnClickListener {
            binding.aadharBackImageLay.visibility = View.GONE
            binding.selectAadharBackImage.visibility = View.VISIBLE
            binding.aadharBackImage.tag = null
        }
        binding.panClose.setOnClickListener {
            binding.panImageLay.visibility = View.GONE
            binding.selectPanImage.visibility = View.VISIBLE
            binding.panImage.tag = null
        }
        setClearError(binding.panNoLay, binding.etPanNo)
        setClearError(binding.panDobLay, binding.etDob)
        setClearError(binding.aadharNoLay, binding.etAadharNo)
        setClearError(binding.addLay, binding.etAdd)
        setClearError(binding.cityLay, binding.etCity)
        setClearError(binding.stateLay, binding.etState)
        setClearError(binding.pincodeLay, binding.etPincode)
        binding.panUpload.setOnClickListener {
            uploadPan()
        }
        binding.aadharUpload.setOnClickListener {
            uploadAadhar()
        }
        setData()
        statewisecode()
        binding.etState.setOnClickListener {
            droupDialog(binding.etState, states as ArrayList<Any>)
        }
    }

    private fun sendAadharOtp(aadhar: String) {
        val param = JsonObject()
        param.addProperty("aadhaar", aadhar)
        param.addProperty("type", "send")
        model.aadharOtpVerify(param) {
            if (it.status == 0) {
                openAadharVerifyPopUp(it.ref_id,it.message)
            }else{
                showToastShort(it.message)
            }
        }
    }


    private fun verifyAadharOtp(refId: String, dialog: BottomSheetDialog) {
        val aadhar = binding.etAadharNo.text.toString().replace(" ", "")
        val param = JsonObject()
        param.addProperty("aadhaar", aadhar)
        param.addProperty("type", "verify")
        param.addProperty("otp", otpBinding.pinView.value.toString())
        param.addProperty("ref_id", refId)
        model.aadharOtpVerify(param) {
            if (it.status == 0) {
                sharedPreference.putBoolean("isAadhar",true)
                timer.cancel()
                binding.btnAadharVerify.visibility=View.GONE
                dialog.dismiss()
                showToastShort(it.message)
            }else{
                sharedPreference.putBoolean("isAadhar",false)
                showToastShort(it.message)
            }
        }
    }

    private fun openAadharVerifyPopUp(refId: String, message: String) {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomSheet)

        _otpBinding = VerifyAadharOtpBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(otpBinding.root)
        dialog.show()

        otpBinding.pinView.setTextColor(Color.parseColor("#25233C"))
        otpBinding.pinView.setTextSize(resources.getDimension(R.dimen._8sp).toInt())
        otpBinding.codeSentTo.text=message

        otpBinding.close.setOnClickListener {
            timer.cancel()
            dialog.dismiss()
        }
        startTImer()
        otpBinding.resend.setOnClickListener {
            dialog.dismiss()
            val aadhar = binding.etAadharNo.text.toString().replace(" ", "")
            try {
                if (otpBinding.pinView.value.isNotEmpty())
                    otpBinding.pinView.value = ""
            } catch (_: Exception) {
            }
            setButtonEnabled(it)
            sendAadharOtp(aadhar)
        }

        otpBinding.loginButton.setOnClickListener {
            setButtonEnabled(it)
            if (otpBinding.pinView.value.length != 6) {
                com.android.masterdistributormdl.utils.showToastShort("Enter 6 Digit OTP")
            } else {
                verifyAadharOtp(refId,dialog)
            }
        }

        // OPTIONAL: Dismiss cleanup to avoid memory leaks
        dialog.setOnDismissListener {
            _otpBinding = null
        }
    }


    fun setButtonEnabled(view: View) {
        com.android.masterdistributormdl.utils.Loading.show2(view.context)
        view.isClickable = false
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(1000))
            withContext(Dispatchers.Main) {
                com.android.masterdistributormdl.utils.Loading.dismiss()
                view.isClickable = true
            }
        }
    }


    private fun startTImer() {
        _otpBinding!!.resend.isEnabled = false
        timer.start()
    }

    private val timer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val second = millisUntilFinished / 1000
//            Log.d(TAG, "OTP-TIMER " + second)
            Log.d("dhuihguhu", "onTick: $second")
            _otpBinding!!.resend.text =
                getHtmlSpanned("Resend Code in <u><font color='#34A853'>${second + 1} seconds</font></u>")
        }

        override fun onFinish() {
            _otpBinding!!.resend.text =
                getHtmlSpanned("Didn't receive code? <u><font color='#34A853'>Resend Code</font></u>")
            _otpBinding!!.resend.isEnabled = true
        }
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

    fun setData() {
        val user = model.user
        if (!user.kyc_status!!.agreement_status) {
            binding.relDistributorApp.visibility = View.VISIBLE
        } else {
            binding.relDistributorApp.visibility = View.GONE
        }
        val comment = user.kyc_status?.comment
        if (comment.isNullOrEmpty()) {
            binding.message.text = ""
            binding.message.visibility = View.GONE
        } else {
            binding.message.text = comment
            binding.message.visibility = View.VISIBLE
        }

        if (user.pan.isNotEmpty()) {
            binding.panCardButton.visibility = View.GONE
        } else {
            binding.panCardButton.visibility = View.VISIBLE
        }
        if (user.aadhaar_detail.aadhaar.isNotEmpty()) {
            binding.aadharCardButton.visibility = View.GONE
        } else {
            binding.aadharCardButton.visibility = View.VISIBLE
        }
        // setMoreDetails(user)
    }

    fun setMoreDetails(user: User) {
        if (user.gender == "Male") {
            binding.maleRB.isChecked = true
        } else if (user.gender == "Female") {
            binding.femaleRB.isChecked = true
        }
        binding.etPanNo.setText(user.pan)
        binding.etDob.setText(printDataFormat(user.dob))
        val aadhar = user.aadhaar_detail
        binding.etAadharNo.setText(aadhar.aadhaar)
        binding.etAdd.setText(aadhar.address)
        binding.etCity.setText(aadhar.city)
        binding.etState.setText(aadhar.state)
        binding.etPincode.setText(aadhar.pincode)
        if (user.pancardimage.startsWith("http")) {
            loadImage(user.pancardimage) { bitmap ->
                if (binding.panImage != null && bitmap != null) {
                    binding.panImageLay.visibility = View.VISIBLE
                    binding.panImage.setImageBitmap(bitmap)
                    binding.panImage.tag = bitmapToFile(requireContext(), bitmap)
                }
            }
        }
        if (aadhar.aadhaarfrontimage.startsWith("http")) {
            loadImage(aadhar.aadhaarfrontimage) { bitmap ->
                if (binding.aadharFrontImage != null && bitmap != null) {
                    binding.aadharFrontImageLay.visibility = View.VISIBLE
                    binding.aadharFrontImage.setImageBitmap(bitmap)
                    binding.aadharFrontImage.tag = bitmapToFile(requireContext(), bitmap)
                }
            }
        }
        if (aadhar.aadhaarbackimage.startsWith("http")) {
            loadImage(aadhar.aadhaarbackimage) { bitmap ->
                if (binding.aadharBackImage != null && bitmap != null) {
                    binding.aadharBackImageLay.visibility = View.VISIBLE
                    binding.aadharBackImage.setImageBitmap(bitmap)
                    binding.aadharBackImage.tag = bitmapToFile(requireContext(), bitmap)
                }
            }
        }
    }

    private fun uploadPan() {
        if (binding.panImage.tag == null || binding.panImage.tag.toString().isEmpty()) {
            AlertError.show(requireContext(), "Please Attach PAN Image") {}
        } else if (!checkPANNo(binding.etPanNo.text.toString())) {
            binding.panNoLay.requestFocus()
            binding.panNoLay.error = " "
        } else if (binding.etDob.text.toString().isEmpty()) {
            binding.panDobLay.requestFocus()
            binding.panDobLay.error = " "
        } else {
            val base64 = imageToBase64(File(binding.panImage.tag.toString()))
            val param = JsonObject()
            param.addProperty("pan", binding.etPanNo.text.toString())
            param.addProperty("dob", dob)
            param.addProperty("panimg", base64)
            model.panDocuments(param) {
                if (it.status == 0) {
                    SuccessAlert.show(requireContext(), it.message) {
                        binding.etPanNo.isEnabled = false
                        binding.etDob.isEnabled = false
                        binding.panUpload.visibility = View.GONE
                        binding.selectPanImage.visibility = View.GONE
                        binding.panClose.visibility = View.GONE
                    }
                } else {
                    AlertError.show(requireContext(), it.message) {}
                }
            }
        }

    }

    private fun uploadAadhar() {
        val aadhar = binding.etAadharNo.text.toString().replace(" ", "")
        if (binding.aadharFrontImage.tag == null || binding.aadharFrontImage.tag.toString()
                .isEmpty()
        ) {
            AlertError.show(requireContext(), "Please attach Aadhaar Front image") {}
        } else if (binding.aadharBackImage.tag == null || binding.aadharBackImage.tag.toString()
                .isEmpty()
        ) {
            AlertError.show(requireContext(), "Please attach Aadhaar Back image") {}
        } else if (!checkAadharNo(aadhar)) {
            binding.aadharNoLay.requestFocus()
            binding.aadharNoLay.error = " "
        } else if (gender.isEmpty()) {
            AlertError.show(requireContext(), "Select Gender") {}
        } else if (binding.aadharBackImage.tag == null || binding.aadharBackImage.tag.toString()
                .isEmpty()
        ) {
            AlertError.show(requireContext(), "Please attach Aadhaar Back image") {}
        } else if (binding.etAdd.text.toString().isEmpty()) {
            binding.addLay.requestFocus()
            binding.addLay.error = " "
        } else if (binding.etCity.text.toString().isEmpty()) {
            binding.cityLay.requestFocus()
            binding.cityLay.error = " "
        } else if (binding.etState.text.toString().isEmpty()) {
            binding.stateLay.requestFocus()
            binding.stateLay.error = " "
        } else if (binding.etPincode.length() != 6) {
            binding.pincodeLay.requestFocus()
            binding.pincodeLay.error = " "
        }
        else if (!sharedPreference.getBoolean("isAadhar")){
            AlertError.show(requireContext(), "Please verify your aadhar number") {}
        }
        else {
            val base64Front = imageToBase64(File(binding.aadharFrontImage.tag.toString()))
            val base64Back = imageToBase64(File(binding.aadharBackImage.tag.toString()))
            val param = JsonObject()
            param.addProperty("frontimg", base64Front)
            param.addProperty("backimg", base64Back)
            param.addProperty("aadhaar", aadhar)
            param.addProperty("gender", gender)
            param.addProperty("address", binding.etAdd.text.toString())
            param.addProperty("pincode", binding.etPincode.text.toString())
            param.addProperty("state", binding.etState.text.toString())
            param.addProperty("city", binding.etCity.text.toString())
            model.aadhaarDocuments(param) {
                if (it.status == 0) {
                    SuccessAlert.show(requireContext(), it.message) {
                        binding.aadharUpload.visibility = View.GONE
                        binding.etAadharNo.isEnabled = false
                        binding.selectAadharFrontImage.visibility = View.GONE
                        binding.selectAadharBackImage.visibility = View.GONE
                        binding.aadharFrontClose.visibility = View.GONE
                        binding.aadharBackClose.visibility = View.GONE
                    }
                } else {
                    AlertError.show(requireContext(), it.message) {}
                }
            }
        }

    }

    private fun startCrop(it: ImageView) {
        imageView = it
        cropImage.launch(CropOptions {
            ///setAspectRatio(500, 400)
            setActivityTitle("Pick Image")
            setRequestedSize(400, 400)
            setAllowFlipping(true)
            setAllowRotation(true)
            setImageSource(includeGallery = true, includeCamera = true)
        })
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent!!
            val path = result.getUriFilePath(requireContext())!!
            setImage(path)
        } else {
            val exception = result.error
            // AlertError.show(requireActivity(), exception!!.localizedMessage)
        }
    }

    private fun setImage(path: String) {
        val bitmap = BitmapFactory.decodeFile(path)
        imageView.setImageBitmap(bitmap)
        imageView.tag = path
        if (imageView == binding.aadharFrontImage) {
            getAadhaarInfo(bitmap)
            binding.aadharFrontImageLay.visibility = View.VISIBLE
            binding.selectAadharFrontImage.visibility = View.GONE
        } else if (imageView == binding.aadharBackImage) {
            getAadhaarBackInfo(bitmap)
            binding.aadharBackImageLay.visibility = View.VISIBLE
            binding.selectAadharBackImage.visibility = View.GONE
        } else if (imageView == binding.panImage) {
            binding.panImageLay.visibility = View.VISIBLE
            binding.selectPanImage.visibility = View.GONE
            getPanInfo(bitmap)
        }
    }

    private fun getAadhaarInfo(bitmap: Bitmap) {
        getTextImage(requireContext(), bitmap) { text ->
            if (text.contains("Male") || text.contains("MALE")) {
                binding.maleRB.isChecked = true
            } else if (text.contains("Female") || text.contains("FEMALE")) {
                binding.femaleRB.isChecked = true
            }
            getAadhaarNoFromText(text) {
                if (it.length == 12) {
                    val no =
                        it.substring(0, 4) + " " + it.substring(4, 8) + " " + it.substring(8, 12)
                    binding.etAadharNo.setText(no)
                }
            }
        }
    }

    private fun getPanInfo(bitmap: Bitmap) {
        getTextImage(requireContext(), bitmap) {
            val text = it
            getPanNoFromText(text) {
                val date = it.dob.split("/")
                if (date.size == 3) {
                    dob = getDateFormat(date[2].toInt(), date[1].toInt(), date[0].toInt())
                    binding.etDob.setText(printDataFormat(dob))
                }
                if (it.pan_no.isNotEmpty()) {
                    binding.etPanNo.setText(it.pan_no)
                }
            }
        }
    }

    private fun getAadhaarBackInfo(bitmap: Bitmap) {
        getTextImage(requireContext(), bitmap) { text ->
            setAadharBackData(text)
        }
    }

    private fun setAadharBackData(str: String) {
        try {
            Log.d(TAG, "PrintData0 :" + str)
            var plain = str.trim().replace("[^/a-zA-Z0-9,]".toRegex(), " ")
            Log.d(TAG, "PrintData1 :" + plain)
            val str = plain.split("Address".toRegex())
            Log.d(TAG, "PrintData2 :" + str[1])
            var pincode = ""
            val str1 = str[1].split(" ")
            for (it in str1) {
                if (it.length == 6 && checkPincode(it)) {
                    pincode = it
                    break
                }
            }
            val pinBefore = str[1].split(pincode.toRegex())

            plain = pinBefore[0]
            Log.d(TAG, "PrintData3 :" + plain)
            plain = plain.trimStart()
            //plain = plain.replace("\\s+".toRegex(), " ")
            Log.d(TAG, "PrintData4 :" + plain)
            val array = plain.split(", ")
            if (array.size > 2) {
                val array = array.toCollection(ArrayList())
                if (array.contains(""))
                    array.remove("")
                val state = array[array.size - 1].trimEnd()
                array.removeAt(array.size - 1)
                val district = array[array.size - 1]
                array.removeAt(array.size - 1)
                val address = array.joinToString(separator = ", ")
                binding.etAdd.setText(address)
                binding.etCity.setText(district)
                binding.etState.setText(state)
                binding.etPincode.setText(pincode)
                Log.d(
                    TAG,
                    "PrintData Address :" + address + ";\nDistrict :" + district + ";\nState :" + state + ";\nPincode :" + pincode + ";"
                )
            }
        } catch (e: Exception) {
        }


    }

    var dob = ""
    private fun datePicker() {
        val calendar = Calendar.getInstance()
        val calender = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.date_dialog_theme,
            { _, year, month, day ->
                dob = getDateFormat(year, month + 1, day)
                binding.etDob.setText(printDataFormat(dob))

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = calender.timeInMillis
        datePickerDialog.show()
    }

    var callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isEnabled) {
                isEnabled = false
                setOnBackResult(requireActivity(), "kyc_update")
            }
        }
    }
}

class KycModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val user = sharedPreference.getUserDist()!!


    fun panDocuments(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().update_pan(param).body()!!
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

    fun setLatLong(lat: Double, lng: Double) {
        sharedPreference.putString(latitude, lat.toString())
        sharedPreference.putString(longitude, lng.toString())
        Log.d(TAG, "Lat-Long " + lat + "," + lng)

    }

    fun aadhaarDocuments(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().update_aadhaar(param).body()!!
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

    fun addBankDetails(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().addBankDetails(param).body()!!
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

    //get bank details by ifsc code
    fun getIfscData(param: JsonObject, result: (IfscResult) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getBankIfscData(param).body()!!
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


    fun aadharOtpVerify(param: JsonObject, result: (ApiResponseAadhar) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().aadharOTPVerify(param).body()!!
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



