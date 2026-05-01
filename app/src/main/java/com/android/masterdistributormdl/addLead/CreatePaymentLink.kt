package com.android.masterdistributormdl.addLead


import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.adapter.DroupAdapter
import com.android.masterdistributormdl.databinding.CreatePaymentLinkBinding
import com.android.masterdistributormdl.gskDistributor.model.Territory
import com.android.masterdistributormdl.gskDistributor.utils.SuccessAlert
import com.android.masterdistributormdl.gskDistributor.utils.isEmailValid
import com.android.masterdistributormdl.home.Home

import com.android.masterdistributormdl.home.HomeModel
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.model.LeadStageData
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar


class CreatePaymentLink : Fragment() {
    private lateinit var binding: CreatePaymentLinkBinding
    lateinit var model: HomeModel
    private var pincodeList = ArrayList<Territory>()
    private var planList = ArrayList<LeadStageData>()
    private var planId = ""
    private var leadId = ""
    private var pincode_count = ""
    private var clientEmail = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.create_payment_link, container, false)
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
        handleRetrofitMessage()
        initView()
        getPlanList()
        initListener()
    }

    private fun getPlanByCity() {
        val param = JsonObject()
        param.addProperty("plan_id", planId)
        model.getCityPlan(param) {
            if (it.status == 0) {
                pincodeList = it.territory
                binding.recPincodeList.visibility=View.VISIBLE
                binding.txtPincodeMessage.visibility=View.VISIBLE
                model.pincodeListAdapter.updateAdapter(pincodeList as ArrayList<Any>)
                binding.recPincodeList.adapter = model.pincodeListAdapter
            }else{
                binding.recPincodeList.visibility=View.GONE
                binding.txtPincodeMessage.visibility=View.GONE
            }
        }
    }

    private fun initView() {
        leadId = requireArguments().getString("leadId") ?: ""
        clientEmail = requireArguments().getString("email") ?: ""
    }

    private fun getUserProfile() {
        model.getUserProfile {
            if (it.status == 0) {
                binding.txtPincode.visibility = View.VISIBLE
                binding.txtPincodeMessage.visibility = View.VISIBLE
                binding.recPincodeList.visibility = View.VISIBLE
                pincodeList = it.data.territory
                model.pincodeListAdapter.updateAdapter(pincodeList as ArrayList<Any>)
                binding.recPincodeList.adapter = model.pincodeListAdapter
            } else {
                binding.txtPincode.visibility = View.GONE
                binding.txtPincodeMessage.visibility = View.GONE
                binding.recPincodeList.visibility = View.GONE
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListener() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            if (binding.txtOpportunity.text.isEmpty()) {
                showToastShort("Please select plan")
            } else if (model.pincodeSelectedList.isEmpty()) {
                showToastShort("Please select at least one pincode")
            } else {
                if (clientEmail.isNotEmpty()){
                    createPaymentLink()
                }else{
                    updateEmail()
                }

            }


        }



        binding.txtOpportunity.setOnClickListener {
            openBottomSheetLead(
                4,
                planList as ArrayList<Any>,
                binding.txtOpportunity,
                "Select Plan"
            )
        }
    }

    //email verify dialog
    private fun updateEmail() {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.email_validation)
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)

        val save = dialog.findViewById<Button>(R.id.save)!!
        val close = dialog.findViewById<ImageView>(R.id.close)!!
        val email = dialog.findViewById<EditText>(R.id.email)!!
        dialog.show()

        close.setOnClickListener {
            dialog.dismiss()
        }
        save.setOnClickListener {
            if (!isEmailValid(email.text.toString())) {
                email.setBackgroundResource(R.drawable.edt_error)
                email.requestFocus()
            }  else {
                val param = JsonObject()
                param.addProperty("lead_id", leadId)
                param.addProperty("type", "lead_email")
                param.addProperty("lead_email", email.text.toString())
                updateEmailApi(dialog,param)
            }
        }
    }

    private fun updateEmailApi(dialog: BottomSheetDialog,param:JsonObject) {
        model.updateLeadStatus(param) {
            if (it.status == 0) {
                dialog.dismiss()
                addActivity("email_update_activity")
            }
        }
    }
    private fun addActivity(type: String) {
        val currentDateAndTime=getInitialDateTime()
        val param = JsonObject()
        param.addProperty("lead_id", leadId)
        param.addProperty("type", type)
        param.addProperty("message","")
        param.addProperty("activity_time", "Today $currentDateAndTime")
        addActivityApi(param)
    }

    private fun addActivityApi(param:JsonObject) {
        model.updateLeadStatus(param) {
            if (it.status == 0) {
                getLeadDetails()
                createPaymentLink()
            }
        }
    }


    private fun getInitialDateTime():String {
        val calendar = Calendar.getInstance()

        // Format time
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (hour >= 12) "PM" else "AM"
        val hourFormatted = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        val timeStr = "${hourFormatted.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
        return timeStr
    }


    private fun createPaymentLink() {
        val selectedListValue = JsonArray()
        for (item in model.pincodeSelectedList) {
            selectedListValue.add(item)
        }
        val countLimit = pincode_count.toIntOrNull() ?: 0
        val selectedPincodeCount = selectedListValue.count()
        if (pincode_count == "City") {
            submitForm(selectedListValue)
        } else {
            if (selectedPincodeCount <= countLimit) {
                submitForm(selectedListValue)
            } else {
                AlertError.show(requireContext(), "You can Select Maximum $pincode_count pincode") {}
            }
        }


    }

    private fun submitForm(selectedListValue: JsonArray) {
        val param = JsonObject()
        param.addProperty("lead_id", leadId)
        param.addProperty("plan_id", planId)
        param.add("ter_pin", selectedListValue)
        model.createPaymentLink(param) {
            if (it.status == 0) {
                SuccessAlert.show(requireContext(), it.message) {
                    (requireActivity() as MainActivity).updateBottom(1, Home())
                }
            } else {
                AlertError.show(requireContext(), it.message) {}
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
                pincode_count = it.pincode_count
                binding.txtPincodeMessage.text="Note: You can Select Minimum 1 and Maximum $pincode_count"
                if (planId == "4") {
                    binding.txtPincodeMessage.text="Note: You can Select Minimum 1 and Maximum Any"
                    getPlanByCity()
                } else {
                    getUserProfile()
                }


            }
        }

        close?.setOnClickListener {
            dialog.dismiss()
        }


    }

    private fun getPlanList() {
        model.getPlanList {
            if (it.status == 0) {
                planList = it.data
            }
        }
    }

    private fun getLeadDetails() {
        val param = JsonObject().apply {
            addProperty("lead_id", leadId)
        }

        model.getLeadStatusData(param) { response ->
            if (response.status == 0) {
                val data = response.data
                clientEmail=data.email
                // Heavy work in background thread
            /*    CoroutineScope(Dispatchers.Default).launch {
                    val data = response.data
                    if (data.opportunity_size.isNotEmpty()) {
//                        binding.txtOpportunity.text = data.opportunity_size
//                        planId = data.opportunity_id
//                        pincode_count = data.pincode_allowed
                    }
                }*/
//                getUserProfile()
            }
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


