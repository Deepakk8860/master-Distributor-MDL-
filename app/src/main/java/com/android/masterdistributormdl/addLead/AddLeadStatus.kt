package com.android.masterdistributormdl.addLead


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.adapter.DroupAdapter
import com.android.masterdistributormdl.databinding.LeadStatusBinding
import com.android.masterdistributormdl.gskDistributor.utils.isEmailValid
import com.android.masterdistributormdl.home.HomeModel
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.model.LeadStageData
import com.android.masterdistributormdl.model.leadstatus.Data
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.addFragment
import com.android.masterdistributormdl.utils.capitalizeWords
import com.android.masterdistributormdl.utils.followUpDate
import com.android.masterdistributormdl.utils.getFormattedDate
import com.android.masterdistributormdl.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.utils.loadImage
import com.android.masterdistributormdl.utils.onBackResult
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class AddLeadStatus : Fragment() {
    private lateinit var binding: LeadStatusBinding
    lateinit var model: HomeModel
    private var leadList = ArrayList<LeadStageData>()
    private var activityList = ArrayList<LeadStageData>()
    var layoutManager: LinearLayout? = null
    private var scheduleList = ArrayList<LeadStageData>()
    private var planList = ArrayList<LeadStageData>()
    private var leadId = ""
    private var displayName = ""
    private var clientMobile = ""
    private var clientWhatsappMobile = ""
    private var clientEmail = ""
    private var leadData: Data? = null
    private var isGmailOpened = false
    private var isCallOpened = false
    private var isMessageOpened = false
    private var isWhatsAppOpened = false
    private var allTimelineActivities = ArrayList<Any>()
    private var currentTimelineList = ArrayList<Any>()
    private val INITIAL_LOAD_SIZE = 10
    private val LOAD_MORE_SIZE = 10
    private var isLoadingMore = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.lead_status, container, false)
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
        initListener()

        // Smooth Entry: Delay API calls slightly to let fragment transition finish
        view.post {
            if (isAdded) {
                getLeadStage()
                getLeadDetails()
                getPlanList()
            }
        }

        getActivityList()
        getScheduleList()
    }

    private fun getPlanList() {
        model.getPlanList{
            if (it.status == 0) {
                planList=it.data
            }
        }
    }

    private fun getScheduleList() {
        scheduleList = arrayListOf(
            LeadStageData("Today", "", "1"),
            LeadStageData("Tomorrow", "", "2"),
            LeadStageData("3 days from now", "", "3"),
            LeadStageData("1 week from now", "", "4"),
            LeadStageData("1 month from now", "", "5"),
            LeadStageData("Select custom date and time", "", "6"),
            LeadStageData("Someday", "", "7"),
            LeadStageData("No Follow Up", "", "8")
        )
    }

    private fun getActivityList() {
        activityList = arrayListOf(
            LeadStageData("Phone Call", R.drawable.iv_phone.toString(), "1", "call_activity"),
            LeadStageData("Message", R.drawable.iv_message.toString(), "2", "message_activity"),
            LeadStageData("Meeting", R.drawable.iv_calender.toString(), "3", "meeting_activity"),
            LeadStageData("Note", R.drawable.iv_notes.toString(), "4", "note_activity")
        )
    }

    private fun initView() {

        binding.swipeRefreshLayout.setOnRefreshListener {
            getLeadDetails()
        }

        leadId = requireArguments().getString("leadId") ?: ""

        onBackResult("add_activity", requireActivity()) {
            getLeadDetails()
        }

        binding.btnLoadMore.setOnClickListener {
            if (!isLoadingMore) {
                loadMoreTimelineData()
            }
        }

    }
    private fun getLeadDetails() {
        val param = JsonObject().apply {
            addProperty("lead_id", leadId)
        }

        model.getLeadStatusData(param) { response ->
            if (response.status == 0) {
                // Heavy work in background thread
                CoroutineScope(Dispatchers.Default).launch {
                    val data = response.data
                    allTimelineActivities = ArrayList(data.activities)

                    // Pre-process formatting in background to keep UI thread light
                    val capitalizedName = data.display_name.capitalizeWords()

                    // Back to Main thread for UI update
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            binding.txtDisplayName.text = capitalizedName
                            setData(data)
                            loadInitialTimelineData()
                        }
                    }
                }
            }
        }
    }

    private fun loadInitialTimelineData() {
        CoroutineScope(Dispatchers.Default).launch {
            val initialList = ArrayList<Any>()
            val initialSize = minOf(allTimelineActivities.size, INITIAL_LOAD_SIZE)
            for (i in 0 until initialSize) {
                initialList.add(allTimelineActivities[i])
            }
            withContext(Dispatchers.Main) {
                currentTimelineList.clear()
                currentTimelineList.addAll(initialList)
                model.timeLineAdapter.updateAdapter(ArrayList(currentTimelineList))
                updateLoadMoreButtonVisibility()
            }
        }
    }

    private fun loadMoreTimelineData() {
        if (currentTimelineList.size >= allTimelineActivities.size) {
            binding.btnLoadMore.visibility = View.GONE
            return
        }

        isLoadingMore = true
        binding.btnLoadMore.isEnabled = false
        binding.btnLoadMore.text = "Loading..."

        CoroutineScope(Dispatchers.Default).launch {
            val currentSize = currentTimelineList.size
            val remaining = allTimelineActivities.size - currentSize
            val nextSize = minOf(remaining, LOAD_MORE_SIZE)
            val newBatch = ArrayList<Any>()
            for (i in 0 until nextSize) {
                newBatch.add(allTimelineActivities[currentSize + i])
            }
            withContext(Dispatchers.Main) {
                currentTimelineList.addAll(newBatch)
                model.timeLineAdapter.updateAdapter(ArrayList(currentTimelineList))
                isLoadingMore = false
                binding.btnLoadMore.isEnabled = true
                binding.btnLoadMore.text = "Load More"
                updateLoadMoreButtonVisibility()
            }
        }
    }

    private fun updateLoadMoreButtonVisibility() {
        if (currentTimelineList.size < allTimelineActivities.size) {
            binding.btnLoadMore.visibility = View.VISIBLE
        } else {
            binding.btnLoadMore.visibility = View.GONE
        }
    }



    private fun setData(it: Data) {
        leadData = it
        clientMobile = it.mobile
        clientEmail = it.email
        clientWhatsappMobile = it.whatsapp_num
        displayName = it.display_name
        // Display name is now set in getLeadDetails pre-processed
        binding.txtMobile.text = it.mobile
        if (it.followup_date.isNotEmpty()) {
            binding.txtFollowsUpDate.text = it.followup_date
        } else {
            binding.txtFollowsUpDate.text = it.followup.toString()
        }
        binding.txtLeadStage.text = it.lead_stage ?: ""
        if (it.opportunity_size.isNotEmpty()) {
            binding.txtOpportunity.text = it.opportunity_size
        }
        binding.txtNotes.text = it.notes ?: ""


    }

    @SuppressLint("QueryPermissionsNeeded")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListener() {
        binding.ivLeadShare.setOnClickListener {
            addFragment(requireActivity(),PreferedMessageSharing())
        }

        binding.ivPaymentLink.setOnClickListener {
            addFragment(requireActivity(),CreatePaymentLink(), bundleOf("leadId" to leadId,"email" to clientEmail, "leadData" to leadData))
        }

        binding.ivCall.setOnClickListener {
            val phoneNumber = clientMobile
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            try {
                isCallOpened = true
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                isCallOpened = false
                showToastShort("Call app not installed")
            }

        }

        binding.ivWhatsApp.setOnClickListener {
            val phoneNumber = "+91$clientMobile" // Should include country code (e.g., "+911234567890")


            // Remove any non-digit characters and ensure it starts with country code
            val cleanNumber = phoneNumber.replace(Regex("[^\\d+]"), "")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$cleanNumber")
            }

            try {
                isWhatsAppOpened=true
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                isWhatsAppOpened=false
                showToastShort("WhatsApp not installed")
            }
        }

        binding.ivMessage.setOnClickListener {
            val phoneNumber = clientMobile

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("sms:$phoneNumber")
                putExtra("sms_body", "") // Optional: add default message
            }

            try {
                isMessageOpened=true
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                isMessageOpened=false
                showToastShort("No SMS app found")
            }
        }


        binding.ivEmail.setOnClickListener {
            if (clientEmail.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(clientEmail))
                    setPackage("com.google.android.gm") // Force Gmail
                }

                try {
                    isGmailOpened = true
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    isGmailOpened = false
                    showToastShort("Gmail app not installed")
                }
            } else {
                isGmailOpened = false
                updateEmail()
//                showToastShort("You don't have an email address saved for this client yet.")
            }
        }



       /* model.timeLineAdapter.setOnclickListener {
            it as Activity
            if (it.type == "call_activity" || it.type == "message_activity" || it.type == "meeting_activity" || it.type == "note_activity") {
                addFragment(
                    requireActivity(),
                    AddActivity(),
                    bundleOf(
                        "activityId" to it.activity_id,
                        "displayName" to displayName,
                        "leadId" to leadId,
                        "type" to it.type,
                        "ApiType" to "Update_Activity"
                    )
                )

            }
        }*/


        binding.backButton.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }

        binding.txtNotes.setOnClickListener {
            openBottomSheetNotes()
        }

        binding.txtLeadStage.setOnClickListener {
            openBottomSheetLead(1, leadList as ArrayList<Any>, binding.txtLeadStage, binding.ivIcon,"Select Lead Stage")
        }

        binding.txtAddActivity.setOnClickListener {
            openBottomSheetLead(
                2,
                activityList as ArrayList<Any>,
                binding.txtAddActivity,
                binding.ivIcon,
                "Add Activity"
            )
        }

        binding.relFollowUpDate.setOnClickListener {
            openBottomSheetLead(
                3,
                scheduleList as ArrayList<Any>,
                binding.txtFollowsUpDate,
                binding.ivIcon,
                "Select Plan"
            )
        }

        binding.txtOpportunity.setOnClickListener {
            openBottomSheetLead(
                4,
                planList as ArrayList<Any>,
                binding.txtOpportunity,
                binding.ivIcon,
                "Select Plan"
            )
        }
        binding.txtFollowsUpDate.setOnClickListener {
            openBottomSheetLead(
                3,
                scheduleList as ArrayList<Any>,
                binding.txtFollowsUpDate,
                binding.ivIcon,
                "Schedule follow up"
            )
        }

//        binding.txtOpportunity.setOnClickListener {
//            openBottomSheetOpportunitySize()
//        }
    }



    private fun getLeadStage() {
        val param = JsonObject()
        model.getLeadStage(param) {
            if (it.status == 0) {
                leadList = it.data
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
            binding.swipeRefreshLayout.isRefreshing=it
            Loading.showHide(requireActivity(), it)
        }
    }

    private fun openBottomSheetOpportunitySize() {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomSheet)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_opportunity_size, null)
        val edtAmount = view.findViewById<EditText>(R.id.edtAmount)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
//            updateOpportunitySize(dialog, edtAmount)
        }


        dialog.setContentView(view)

        dialog.show()

        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun openBottomSheetActivityLog() {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomSheet)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_opportunity_size, null)
        val edtAmount = view.findViewById<EditText>(R.id.edtAmount)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
//            updateOpportunitySize(dialog, edtAmount)
        }


        dialog.setContentView(view)

        dialog.show()

        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            dialog.dismiss()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openBottomSheetLead(
        viewType: Int,
        array: ArrayList<Any>,
        txtLeadStatus: AppCompatTextView,
        ivIcon: AppCompatImageView,
        titleMain: String
    ) {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomSheet)
        dialog.setContentView(R.layout.bottom_sheet_title_icon)
        dialog.show()
        val close = dialog.findViewById<ImageView>(R.id.ivClose)
        val title = dialog.findViewById<TextView>(R.id.tvTitle)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        title?.text=titleMain

        val adapter = DroupAdapter(viewType)
        adapter.updateAdapter(array)
        recyclerView?.adapter = adapter
        recyclerView?.addItemDecoration(
            DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
        )



        adapter.setOnclickListener {
            dialog.dismiss()
            if (viewType == 1) {
                it as LeadStageData
                ivIcon.visibility = View.VISIBLE
                txtLeadStatus.text = it.name
                txtLeadStatus.tag = it.id
                loadImage(ivIcon, it.icon)
                updateLeadStage(dialog, it.name)
            } else if (viewType == 2) {
                it as LeadStageData
                dialog.dismiss()
                addFragment(
                    requireActivity(),
                    AddActivity(),
                    bundleOf(
                        "activityId" to it.id,
                        "displayName" to displayName,
                        "leadId" to leadId,
                        "type" to it.type,
                        "ApiType" to "add_Activity"
                    )
                )
            } else if (viewType == 3) {
                it as LeadStageData
                dialog.dismiss()
//                txtLeadStatus.text = it.name
                txtLeadStatus.tag = it.id
                val calculatedDate = getFormattedDate(it.name)
                val followUpString = getFollowUpText(it.name)
                binding.txtFollowUpStatus.text = followUpString

//                binding.txtFollowsUpDate.text=calculatedDate
//                binding.txtFollowUpTime.text="8:00 AM"
                if (it.id == "6") {
                    followUpDate = ""
                    openDatePickerLead(
                        dialog,
                        requireContext(),
                        it.name,
                        binding.txtFollowsUpDate,
                        binding.txtFollowUpTime
                    )

                } else if (it.id == "7") {
                    binding.txtFollowsUpDate.text = "Someday"
                    updateFollowUpDate(dialog, it.name, calculatedDate)
//                    binding.txtFollowUpTime.text=""
                } else if (it.id == "8") {
                    binding.txtFollowsUpDate.text = "No Follow Up"
                    updateFollowUpDate(dialog, it.name, calculatedDate)
//                    binding.txtFollowUpTime.text=""
                } else if (it.id == "1" || it.id == "2" || it.id == "3" || it.id == "4" || it.id == "5") {
                    updateFollowUpDate(dialog, it.name, calculatedDate)
                }

            }

            if (viewType == 4) {
                it as LeadStageData
                txtLeadStatus.text = it.name
                txtLeadStatus.tag = it.id
                updateOpportunitySize(dialog,it.name,it.id)
            }
        }

        close?.setOnClickListener {
            dialog.dismiss()
        }


    }

    private fun getFollowUpText(option: String): String {
        return when (option) {
            "Today" -> "Follow UP Today"
            "Tomorrow" -> "Follow UP Tomorrow"
            "3 days from now" -> "Follow UP IN 3 days"
            "1 week from now" -> "Follow UP IN 1 week"
            "1 month from now" -> "Follow UP IN 1 month"
            else -> "Follow UP"
        }
    }

    fun openDatePickerLead(
        dialog: BottomSheetDialog,
        requireContext: Context,
        value: String,
        txtDate: TextView,
        txtTime: TextView
    ) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(requireContext, { _, selectedYear, selectedMonth, selectedDay ->
                // Format date: 12 May 2025
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                }

                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedCalendar.time)
                txtDate.text = formattedDate

                // Open time picker after date selection
                openTimePickerLead(dialog, requireContext, value, txtTime, formattedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    fun openTimePickerLead(
        dialog: BottomSheetDialog,
        requireContext: Context,
        value: String,
        txtTime: TextView,
        formattedDate: String
    ) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(requireContext, { _, selectedHour, selectedMinute ->
            // Format time
            val amPm = if (selectedHour >= 12) "PM" else "AM"
            val hourFormatted =
                if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour
            val timeStr = "${hourFormatted.toString().padStart(2, '0')}:${
                selectedMinute.toString().padStart(2, '0')
            } $amPm"

            // Set time in txtTime
            txtTime.text = timeStr
            followUpDate = "$formattedDate $timeStr"
            updateFollowUpDate(dialog, value, followUpDate)

        }, hour, minute, false)

        timePickerDialog.show()
    }

    private fun openBottomSheetNotes() {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomSheet)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_notes, null)
        val edtNotes = view.findViewById<EditText>(R.id.edtNotes)
        edtNotes.requestFocus()
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            updateNotes(dialog, edtNotes)
        }


        dialog.setContentView(view)

        dialog.show()

        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun openBottomSheetSaveActivity(message: String, icon: Int, type: String) {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomSheet)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_save_activity, null)
        val llAddActivity = view.findViewById<LinearLayout>(R.id.llAddActivity)
        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val ivImage = view.findViewById<ImageView>(R.id.ivImage)
        val btnDiscard = view.findViewById<Button>(R.id.btnDiscard)
        val edtNotes = view.findViewById<EditText>(R.id.edtNotes)
        txtTitle.text=message
        ivImage.setImageResource(icon)
        llAddActivity.setOnClickListener {
            addActivity(dialog,edtNotes,type)
        }

        btnDiscard.setOnClickListener {
            dialog.dismiss()
        }


        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()

        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            addActivity(dialog,edtNotes,type)
        }

    }

    private fun updateNotes(dialog: BottomSheetDialog, value: EditText) {
        val param = JsonObject()
        param.addProperty("lead_id", leadId)
        param.addProperty("type", "notes")
        param.addProperty("notes", value.text.toString())
        updateApiCall(dialog, param, "note_activity",value.text.toString())
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

    private fun updateEmailClient(dialog: BottomSheetDialog,param: JsonObject) {
        addActivityApi(dialog,param)
    }


    private fun updateLeadStage(dialog: BottomSheetDialog, value: String) {
        val param = JsonObject()
        param.addProperty("lead_id", leadId)
        param.addProperty("type", "lead_stage")
        param.addProperty("lead_stage", value)
        updateApiCall(dialog, param, "lead_stage_activity", "")
    }

    private fun updateFollowUpDate(
        dialog: BottomSheetDialog,
        value: String,
        calculatedDate: String
    ) {
        val param = JsonObject()
        param.addProperty("lead_id", leadId)
        param.addProperty("type", "followup")
        param.addProperty("followup", value)
        if (value == "Someday" || value == "No Follow Up") {
            param.addProperty("followup_date", "")
        } else {
            param.addProperty("followup_date", calculatedDate)

        }
        updateApiCall(dialog, param, "follow_up_activity", "")
    }

    private fun updateOpportunitySize(dialog: BottomSheetDialog, value: String, id: String) {
        val param = JsonObject()
        param.addProperty("lead_id", leadId)
        param.addProperty("type", "opportunity")
        param.addProperty("opportunity_size", value)
        param.addProperty("opportunity_id", id)
        updateApiCall(dialog, param, "opportunity_size_activity", "")
    }

    private fun addActivity(dialog: BottomSheetDialog,edtNotes: EditText, type: String) {
        val currentDateAndTime=getInitialDateTime()
        val param = JsonObject()
        param.addProperty("lead_id", leadId)
        param.addProperty("type", type)
        param.addProperty("message", edtNotes.text.toString())
        param.addProperty("activity_time", "Today $currentDateAndTime")
        addActivityApi(dialog,param)
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

    private fun addActivityApi(dialog: BottomSheetDialog,param:JsonObject) {
        model.updateLeadStatus(param) {
            if (it.status == 0) {
                dialog.dismiss()
                getLeadDetails()
            }
        }
    }
    private fun addActivityApi(param:JsonObject) {
        model.updateLeadStatus(param) {
            if (it.status == 0) {
                getLeadDetails()
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

    private fun updateApiCall(
        dialog: BottomSheetDialog,
        param: JsonObject,
        type: String,
        message: String
    ) {
        model.updateLeadStatus(param) {
            if (it.status == 0) {
                val currentDateAndTime=getInitialDateTime()
                val param2 = JsonObject()
                param2.addProperty("lead_id", leadId)
                param2.addProperty("type", type)
                param2.addProperty("message", message)
                param2.addProperty("activity_time", "Today $currentDateAndTime")
                addActivityApi(dialog,param2)
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



    override fun onResume() {
        super.onResume()

        // Check if user is returning from Gmail
        if (isGmailOpened) {
            isGmailOpened = false // Reset flag
            openBottomSheetSaveActivity("Message via Email",R.drawable.iv_message_white,"email_activity")
        } // Check if user is returning from Gmail
        if (isCallOpened) {
            isCallOpened = false // Reset flag
            openBottomSheetSaveActivity("Phone Call",R.drawable.iv_call_white,"call_activity")
        } // Check if user is returning from Gmail
        if (isMessageOpened) {
            isMessageOpened = false // Reset flag
            openBottomSheetSaveActivity("Message via SMS",R.drawable.iv_message_white,"message_activity")
        } // Check if user is returning from Gmail
        if (isWhatsAppOpened) {
            isWhatsAppOpened = false // Reset flag
            openBottomSheetSaveActivity("Message via WhatsApp",R.drawable.iv_message_white,"whatsApp_activity")
        }
    }

}


