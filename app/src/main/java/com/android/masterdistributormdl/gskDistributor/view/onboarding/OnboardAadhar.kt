package com.android.masterdistributormdl.gskDistributor.view.onboarding

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.cropper.CropImageContract
import com.android.masterdistributormdl.cropper.CropOptions
import com.android.masterdistributormdl.databinding.OnboardAadharBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.gsk.distributor.model.StateItem
import com.android.masterdistributormdl.gskDistributor.utils.AddTextWatcher
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.utils.bitmapToFile
import com.android.masterdistributormdl.gskDistributor.utils.checkAadharNo
import com.android.masterdistributormdl.gskDistributor.utils.checkPincode
import com.android.masterdistributormdl.gskDistributor.utils.getAadhaarNoFromText
import com.android.masterdistributormdl.gskDistributor.utils.getTextImage
import com.android.masterdistributormdl.gskDistributor.utils.imageToBase64
import com.android.masterdistributormdl.gskDistributor.utils.loadImage
import com.android.masterdistributormdl.gskDistributor.utils.replaceFragment
import com.android.masterdistributormdl.gskDistributor.utils.setClearError
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.VideoKyc
import java.io.File


class OnboardAadhar : Fragment() {
    private lateinit var binding: OnboardAadharBinding
    lateinit var model: OnboardModel
    lateinit var image: ImageView
    var gender = ""
    val states = ArrayList<StateItem>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.onboard_aadhar, container, false)
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
        setClearError(binding.aadharNoLay, binding.etAadharNo)
        setClearError(binding.addLay, binding.etAdd)
        setClearError(binding.cityLay, binding.etCity)
        setClearError(binding.stateLay, binding.etState)
        setClearError(binding.pincodeLay, binding.etPincode)
        binding.frontImageLay.visibility = View.GONE
        binding.backImageLay.visibility = View.GONE
        binding.etAadharNo.addTextChangedListener(AddTextWatcher())
        binding.selectFrontImage.setOnClickListener { startCrop(binding.frontImage) }
        binding.selectBackImage.setOnClickListener { startCrop(binding.backImage) }
        binding.frontClose.setOnClickListener {
            binding.frontImageLay.visibility = View.GONE
            binding.frontImage.tag = null
        }
        binding.backClose.setOnClickListener {
            binding.backImageLay.visibility = View.GONE
            binding.backImage.tag = null
        }
        binding.etState.setOnClickListener {
            droupDialog(binding.etState, states as ArrayList<Any>)
        }
        (requireActivity() as OnboardActivity).keyboard(binding.mainLayout, binding.bottomLay)
        binding.genderRG.setOnCheckedChangeListener { radioGroup, i ->
            if (binding.maleRB.isChecked) {
                gender = "Male"
                binding.maleRB.buttonTintList = (ColorStateList.valueOf(Color.parseColor("#F86202")))
                binding.femaleRB.buttonTintList = ColorStateList.valueOf(Color.parseColor("#1D3667"))
            } else {
                gender = "Female"
                binding.maleRB.buttonTintList = ColorStateList.valueOf(Color.parseColor("#1D3667"))
                binding.femaleRB.buttonTintList = ColorStateList.valueOf(Color.parseColor("#F86202"))
            }
        }
        // maleRB.isChecked = true
        binding.next.setOnClickListener { check() }
        binding.back.setOnClickListener {
            replaceFragment(requireActivity(), OnboardPan())
        }
        statewisecode()
        setData()
    }

    fun setData() {
        val user = model.getUser()
        (requireActivity() as OnboardActivity).setNavigation(user.kyc_status!!, 4,user.video)
        val aadhar = user.aadhaar_detail
        if (aadhar.aadhaarfrontimage.startsWith("http")) {
            loadImage(aadhar.aadhaarfrontimage) { bitmap ->
                if (binding.frontImage != null && bitmap != null) {
                    binding.frontImageLay.visibility = View.VISIBLE
                    binding.frontImage.setImageBitmap(bitmap)
                    binding.frontImage.tag = bitmapToFile(requireContext(), bitmap)
                }
            }
        }

        if (aadhar.aadhaarbackimage.startsWith("http")) {
            loadImage(aadhar.aadhaarbackimage) { bitmap ->
            if (binding.backImage != null && bitmap != null) {
                binding.backImageLay.visibility = View.VISIBLE
                binding.backImage.setImageBitmap(bitmap)
                binding.backImage.tag = bitmapToFile(requireContext(), bitmap)
            }
        }
        }


        binding.etAadharNo.setText(aadhar.aadhaar)
        binding.etAdd.setText(aadhar.address)
        binding.etCity.setText(aadhar.city)
        getStateByStateCode(aadhar.state)

        binding.etPincode.setText(aadhar.pincode)
        if (user.gender == "Male") {
            binding.maleRB.isChecked = true
        } else if (user.gender == "Female") {
            binding.femaleRB.isChecked = true
        }
    }

    private fun statewisecode() {
        model.statewisecode {
            states.clear()
            if (it.status == 0) {
                states.addAll(it.state)
            } else {
                AlertError.show(requireContext(), it.message) {

                }
            }
        }
    }

    private fun getStateByStateCode(stateCode: String) {
        model.statewisecode {
            if (it.status == 0) {
                val matchingStateItem = it.state.find { stateItem -> stateItem.gst_code ==  stateCode}
                binding.etState.setText(matchingStateItem?.gst_state)

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
        val sheet = dialog.findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet)
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

    fun check() {
        val aadhar = binding.etAadharNo.text.toString().replace(" ", "")
        if (binding.frontImage.tag == null || binding.frontImage.tag.toString().isEmpty()) {
            AlertError.show(requireContext(), "Please attach Aadhaar Front image") {}
        } else if (!checkAadharNo(aadhar)) {
            binding.aadharNoLay.requestFocus()
            binding.aadharNoLay.error = " "
        } else if (gender.isEmpty()) {
            AlertError.show(requireContext(), "Select Gender") {}
        } else if (binding.backImage.tag == null || binding.backImage.tag.toString().isEmpty()) {
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
        } else {
            update_aadhaar(aadhar)
        }
    }

    fun update_aadhaar(aadhar: String) {
        val frontimg = imageToBase64(File(binding.frontImage.tag.toString()))
        val backimg = imageToBase64(File(binding.backImage.tag.toString()))
        val param = JsonObject()
        param.addProperty("frontimg", frontimg)
        param.addProperty("backimg", backimg)
        param.addProperty("aadhaar", aadhar)
        param.addProperty("gender", gender)
        param.addProperty("address", binding.etAdd.text.toString())
        param.addProperty("pincode", binding.etPincode.text.toString())
        param.addProperty("state", binding.etState.text.toString())
        param.addProperty("city", binding.etCity.text.toString())
        model.update_aadhaar(param) {
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
                replaceFragment(requireActivity(), VideoKyc())
            }
            else if (it.status==100){
                //handle maintenance
            }
            else {
                AlertError.show(requireActivity(), it.message) {}
            }
        }
    }

    private fun startCrop(it: ImageView) {
        image = it
        cropImage.launch(CropOptions {
            ///setAspectRatio(500, 400)
            setActivityTitle("Pick Image")
            setRequestedSize(700, 700)
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
        image.setImageBitmap(bitmap)
        image.tag = path
        if (image == binding.frontImage) {
            getAadhaarFrontInfo(bitmap)
            binding.frontImageLay.visibility = View.VISIBLE
        } else if (image == binding.backImage) {
            getAadhaarBackInfo(bitmap)
            binding.backImageLay.visibility = View.VISIBLE
        }
    }

    private fun getAadhaarFrontInfo(bitmap: Bitmap) {
        getTextImage(requireContext(), bitmap) { text ->
            //  val text = text.trim().replace("[^/a-zA-Z0-9]".toRegex(), " ")
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
                /* Log.d(
                     TAG,
                     "PrintData Address :" + address+ ";\nDistrict :" + district + ";\nState :" + state+  ";\nPincode :" + pincode+";"
                 )*/
            }
        } catch (e: Exception) {
        }


    }


}





