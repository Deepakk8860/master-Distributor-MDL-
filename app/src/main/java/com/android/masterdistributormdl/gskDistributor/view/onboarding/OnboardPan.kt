package com.android.masterdistributormdl.gskDistributor.view.onboarding

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.cropper.CropImageContract
import com.android.masterdistributormdl.cropper.CropOptions
import com.android.masterdistributormdl.databinding.OnboardPanBinding
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.bitmapToFile
import com.android.masterdistributormdl.gskDistributor.utils.checkPANNo
import com.android.masterdistributormdl.gskDistributor.utils.getDateFormat
import com.android.masterdistributormdl.gskDistributor.utils.getPanNoFromText
import com.android.masterdistributormdl.gskDistributor.utils.getTextImage
import com.android.masterdistributormdl.gskDistributor.utils.imageToBase64
import com.android.masterdistributormdl.gskDistributor.utils.loadImage
import com.android.masterdistributormdl.gskDistributor.utils.printDataFormat
import com.android.masterdistributormdl.gskDistributor.utils.replaceFragment
import com.android.masterdistributormdl.gskDistributor.utils.setClearError
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort

import java.io.File
import java.util.Calendar


class OnboardPan : Fragment() {
    lateinit var model: OnboardModel
    private lateinit var binding: OnboardPanBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.onboard_pan, container, false)
        model = ViewModelProvider(this)[OnboardModel::class.java]
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
            (requireActivity() as OnboardActivity).setHeader("Onboard")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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
        model.errorMessage.observe(viewLifecycleOwner) {
            AlertError.show(requireContext(), it) {}
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireContext(), it)
        }
        setClearError(binding.panNoLay, binding.etPanNo)
        setClearError(binding.panDobLay,binding.etPanDob)
        binding.etPanDob.setOnClickListener { datePicker() }
        binding.pickPanImage.setOnClickListener { startCrop() }
        binding.panImageLay.visibility = View.GONE
        binding.back.setOnClickListener {
            replaceFragment(requireActivity(), OnboardAddress())
        }
        binding.panClose.setOnClickListener {
            binding.panImageLay.visibility = View.GONE
            binding.panImage.tag = null
        }
        setData()
        (requireActivity() as OnboardActivity).keyboard(binding.mainLayout,binding.bottomLay)
        binding.next.setOnClickListener { check() }
    }

    fun setData() {
        val user = model.getUser()
        binding.etPanNo.setText(user.pan)
        dob = user.dob
        binding.etPanDob.setText(printDataFormat(dob))
        if (user.pancardimage.startsWith("http")) {
            loadImage(user.pancardimage) { bitmap ->
                if (binding.panImage != null && bitmap != null) {
                    binding.panImageLay.visibility = View.VISIBLE
                    binding.panImage.setImageBitmap(bitmap)
                    binding.panImage.tag = bitmapToFile(requireContext(), bitmap)
                }
            }
        }
        (requireActivity() as OnboardActivity).setNavigation(user.kyc_status!!, 3,user.video)
    }

    fun check() {
        if (binding.panImage.tag == null || binding.panImage.tag.toString().isEmpty()) {
            AlertError.show(requireContext(), "Please Attach PAN Image") {}
        } else if (!checkPANNo(binding.etPanNo.text.toString())) {
            binding.panNoLay.requestFocus()
            binding.panNoLay.error = " "
        } else if (binding.etPanDob.text.toString().isEmpty()) {
            binding.panDobLay.requestFocus()
            binding.panDobLay.error = " "
        } else {
            update_pan()
        }
    }

    fun update_pan() {
        val file = File(binding.panImage.tag.toString())
        val base64 = imageToBase64(file)
        val param = JsonObject()
        param.addProperty("pan", binding.etPanNo.text.toString())
        param.addProperty("dob", dob)
        param.addProperty("panimg", base64)
        model.update_pan(param) {
            if (it.status == 0) {
                getUserProfile()
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }

    }

    fun getUserProfile() {
        model.getUserProfile {
            if (it.status == 0) {
                model.save(it.data)
                replaceFragment(requireActivity(), OnboardAadhar())
            }
            else if (it.status==100){
                //handle maintenance
            }
            else {
                AlertError.show(requireActivity(), it.message) {}
            }
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
                binding.etPanDob.setText(printDataFormat(dob))

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = calender.timeInMillis
        datePickerDialog.show()
    }

    private fun startCrop() {
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
        binding.panImageLay.visibility = View.VISIBLE
        val bitmap = BitmapFactory.decodeFile(path)
        binding.panImage.setImageBitmap(bitmap)
        binding.panImage.tag = path
        getPanInfo(bitmap)
    }

    private fun getPanInfo(bitmap: Bitmap) {
        getTextImage(requireContext(), bitmap) {
            val text = it
            getPanNoFromText(text) {
                val date = it.dob.split("/")
                if (date.size == 3) {
                    dob = getDateFormat(date[2].toInt(), date[1].toInt(), date[0].toInt())
                    binding.etPanDob.setText(printDataFormat(dob))
                }

                if (it.pan_no.isNotEmpty()){
                    binding.etPanNo.setText(it.pan_no)
                }

            }
        }
    }
}





