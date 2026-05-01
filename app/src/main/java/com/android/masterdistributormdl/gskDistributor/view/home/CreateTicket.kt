package com.android.masterdistributormdl.gskDistributor.view.home

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.cropper.CropImageContract
import com.android.masterdistributormdl.cropper.CropOptions
import com.android.masterdistributormdl.databinding.CreateTicketBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.SpinnerAdapter
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.SuccessAlert
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.imageToBase64
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import java.io.File


class CreateTicket : Fragment() {
    lateinit var model: SupportModel
    private lateinit var binding: CreateTicketBinding
    var base64 = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.create_ticket, container, false)
        model = ViewModelProvider(this)[SupportModel::class.java]
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
        binding.back.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }
        binding.save.setOnClickListener {
            validation()
        }
        setClearError(binding.typeInputLay, binding.type)
        setClearError(binding.subjectInputLay, binding.subject)
        //  setClearError(descriptionInputLay, description)
        binding.type.setOnClickListener { binding.spinner.performClick() }
        binding.attachment.setOnClickListener { startCrop() }
        binding.imageLay.visibility = View.GONE
        binding.close.setOnClickListener {
            binding.imageLay.visibility = View.GONE
            binding.image.setImageBitmap(null)
            base64 = ""
        }

        binding.description.addTextChangedListener {
            binding.wordCount.text = "" + it?.length + "/500"
            binding.descriptionInputLay.error = null
            if (it.isNullOrEmpty()) {
                binding.description.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._12sp)
                )
            } else {
                binding.description.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._14sp)
                )
            }
        }


        getTicketType()
    }

    private fun getTicketType() {
        model.getTicketType {
            if (it.status == 0) {
                val list = ArrayList<String>()
                list.add("Select Ticket Type")
                list.addAll(it.data)
                setSpinner(list)
            } else {
                requireActivity().onBackPressed()
            }
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent!!
            val filePath = result.getUriFilePath(requireContext())
            val file = File(filePath)
            binding.imageLay.visibility = View.VISIBLE
            val bitmap = BitmapFactory.decodeFile(filePath)
            binding.image.setImageBitmap(bitmap)
            base64 = imageToBase64(file)
            file.delete()
        } else {
            val exception = result.error
            // AlertError.show(requireActivity(), exception!!.localizedMessage)
        }
    }

    private fun startCrop() {
        cropImage.launch(CropOptions {
            /*  setAspectRatio(500, 500)*/
            setActivityTitle("Pick Image")
            setRequestedSize(500, 500)
            setAllowFlipping(true)
            setAllowRotation(true)
            setImageSource(includeGallery = true, includeCamera = true)
        })
    }

    private fun setSpinner(list: ArrayList<String>) {
        val adapter = SpinnerAdapter(requireContext(), list)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                binding.typeInputLay.boxStrokeColor = Color.GREEN
                binding.type.setText(list[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

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
        if (binding.spinner.selectedItemPosition == 0) {
            binding.typeInputLay.requestFocus()
            binding.typeInputLay.error = " "
        } else if (binding.type.text.toString().isEmpty()) {
            binding.typeInputLay.requestFocus()
            binding.typeInputLay.error = " "
        } else if (binding.subject.text.toString().isEmpty()) {
            binding.subjectInputLay.requestFocus()
            binding.subjectInputLay.error = " "
        } else if (binding.description.length() < 50) {
            binding.descriptionInputLay.requestFocus()
            binding.descriptionInputLay.error = " "
            showToastShort("Please enter minimum 50 characters")
        } else {
            createTicket()
        }

    }


    fun createTicket() {
        val param = JsonObject()
        param.addProperty("type", binding.type.text.toString())
        param.addProperty("subject", binding.subject.text.toString())
        param.addProperty("description", binding.description.text.toString())
        param.addProperty("attachment", base64)
        model.createTicket(param) {
            if (it.status == 0) {
                SuccessAlert.show(requireContext(), it.message) {
                    requireActivity().onBackPressed()
                }
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }
    }


}









