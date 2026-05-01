package com.android.masterdistributormdl.addLead


import android.os.Build
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.FragmentAddActivityBinding

import com.android.masterdistributormdl.home.HomeModel
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.getFormattedDate
import com.android.masterdistributormdl.utils.getFormattedDateTime
import com.android.masterdistributormdl.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.utils.openDatePicker
import com.android.masterdistributormdl.utils.setOnBackResult
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import java.util.Calendar


class AddActivity : Fragment() {
    private lateinit var binding: FragmentAddActivityBinding
    lateinit var model: HomeModel
    private var leadId=""
    private var type=""
    private var apiType=""
    private var activityId=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_activity, container, false)
        model = ViewModelProvider(this)[HomeModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        initView()
        handleRetrofitMessage()
        initListener()
        handleDateTime()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleDateTime() {
        if (apiType=="add_Activity"){
            setInitialDateTime()
        }else{
            getActivityDetails()
        }
    }

    private fun setInitialDateTime() {
        val calendar = Calendar.getInstance()

        // Format time
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (hour >= 12) "PM" else "AM"
        val hourFormatted = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        val timeStr = "${hourFormatted.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"

        // Set values
        binding.txtDate.text = "Today"
        binding.txtTime.text = timeStr
    }


    private fun initListener() {
        binding.llAddActivity.setOnClickListener {
            addActivity()
        }

        binding.ivClose.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }

        binding.timeSection.setOnClickListener {
            openDatePicker(requireContext(),binding.txtDate,binding.txtTime)
        }

    }


    private fun addActivity() {
        val param = JsonObject()
        param.addProperty("lead_id", leadId)
        param.addProperty("type", type)
        param.addProperty("message", binding.noteInput.text.toString())
        param.addProperty("activity_time", "${binding.txtDate.text.toString()} ${binding.txtTime.text.toString()}")
        if (apiType=="add_Activity"){
            addActivityApi(param)
        }else{
            updateActivity(param)
        }

    }

    private fun updateActivity(param:JsonObject) {
        param.addProperty("activity_id",activityId)
        model.updateActivity(param) {
            if (it.status == 0) {
                setOnBackResult(requireActivity(),"add_activity")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getActivityDetails() {
        val param=JsonObject()
        param.addProperty("activity_id",activityId)
        param.addProperty("lead_id",leadId)
        param.addProperty("type",type)
        model.getActivityDetails(param) {
            if (it.status == 0) {
                binding.noteInput.setText(it.data.
                sub_remark)
               val formatedDate= getFormattedDateTime(it.data.activity_time)
                Log.d("ffughguhgugt", "getActivityDetails1: ${it.data.activity_time}")
                Log.d("ffughguhgugt", "getActivityDetails2: $formatedDate")
                binding.txtTime.visibility=View.GONE
                binding.txtDate.text=formatedDate
            }
        }
    }

    private fun addActivityApi(param:JsonObject) {
        model.updateLeadStatus(param) {
            if (it.status == 0) {
                setOnBackResult(requireActivity(),"add_activity")
            }
        }
    }


    private fun initView() {
        val displayName = requireArguments().getString("displayName") ?: ""
        activityId = requireArguments().getString("activityId") ?: ""
        leadId = requireArguments().getString("leadId") ?: ""
        type = requireArguments().getString("type") ?: ""
        apiType = requireArguments().getString("ApiType") ?: ""
        if (apiType=="Update_Activity"){
            binding.txtButton.text= getString(R.string.save_activity)
        }
        binding.activityTitle.text="Activity With $displayName"
        when (type) {
            "call_activity" -> {
                setNameImage("Phone Call",R.drawable.iv_call_white)
            }
            "message_activity" -> {
                setNameImage("Message",R.drawable.iv_message_white)
            }
            "meeting_activity" -> {
                setNameImage("Meeting",R.drawable.iv_cal_white)
            }
            "note_activity" -> {
                setNameImage("Note",R.drawable.iv_doc_white)
            }
        }

    }




    private fun setNameImage(name: String, image: Int){
        binding.txtTitleName.text = name
        binding.ivTitleImage.setImageResource(image)
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


