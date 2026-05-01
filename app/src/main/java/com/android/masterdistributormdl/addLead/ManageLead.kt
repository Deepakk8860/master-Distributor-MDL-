package com.android.masterdistributormdl.addLead


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
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
import com.android.masterdistributormdl.databinding.ManageClientBinding

import com.android.masterdistributormdl.home.HomeModel
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.model.LeadStageData
import com.android.masterdistributormdl.model.lead.Data
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.addFragment
import com.android.masterdistributormdl.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ManageLead : Fragment() {
    private lateinit var binding: ManageClientBinding
    lateinit var model: HomeModel
    private var leadList = ArrayList<Data>()
    private var filterList = ArrayList<String>()
    private var leadListDropDown = ArrayList<LeadStageData>()
    private var selectedLeadsItem="all"
    private var selectedStatusItem=""
    private var isNew=false
    private var isToday=false
    private var isCurrentMonth=false
    private var isLastMonth=false
    private var selectedSingleItem=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.manage_client, container, false)
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
        getClientList()
        getLeadStage()
    }

    private fun getLeadStage() {
        val param = JsonObject()
        model.getLeadStage(param) {
            if (it.status == 0) {
                leadListDropDown = it.data
            }
        }
    }


    // Function to reset all to unselected
    private fun resetBackgrounds() {
        binding.relNew.setBackgroundResource(R.drawable.bg_toggle_unselected)
        binding.relToday.setBackgroundResource(R.drawable.bg_toggle_unselected)
        binding.relLastMonth.setBackgroundResource(R.drawable.bg_toggle_unselected)
        binding.relCurrentMonth.setBackgroundResource(R.drawable.bg_toggle_unselected)

        binding.tvCount.setTextColor(Color.parseColor("#8000D8"))
        binding.tvTodayCount.setTextColor(Color.parseColor("#8000D8"))
        binding.tvLastMonthCount.setTextColor(Color.parseColor("#8000D8"))
        binding.tvCurrentMonthCount.setTextColor(Color.parseColor("#8000D8"))

        binding.tvNew.setTextColor(Color.parseColor("#000000"))
        binding.tvToday.setTextColor(Color.parseColor("#000000"))
        binding.tvLastMonth.setTextColor(Color.parseColor("#000000"))
        binding.tvCurrentMonth.setTextColor(Color.parseColor("#000000"))

        binding.tvCount.setBackgroundResource(R.drawable.text_background_unselected)
        binding.tvTodayCount.setBackgroundResource(R.drawable.text_background_unselected)
        binding.tvLastMonthCount.setBackgroundResource(R.drawable.text_background_unselected)
        binding.tvCurrentMonthCount.setBackgroundResource(R.drawable.text_background_unselected)
    }

    private fun resetBackgroundsStatus() {
        binding.relStatus.setBackgroundResource(R.drawable.bg_toggle_unselected)
        binding.txtStatus.setTextColor(Color.parseColor("#000000"))
        binding.ivDropStatus.setBackgroundResource(R.drawable.ic_arrow_down_black)
    }


    private fun initView() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM ''yy", Locale.ENGLISH)
        val formattedDate = dateFormat.format(calendar.time)
        binding.tvCurrentMonth.text = formattedDate

        filterList.add("all")
        binding.swipeRefreshLayout.setOnRefreshListener {
            clearFilter()
        }
        binding.recClient.addItemDecoration(
            DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
        )
    }

    private fun clearFilter(){
        selectedLeadsItem="all"
        selectedStatusItem=""
        selectedSingleItem=""
        binding.txtLeadStatus.text="All Leads"
        binding.txtStatus.text="Status"
        resetBackgrounds()
        resetBackgroundsStatus()
        getClientList()
    }

    @SuppressLint("ResourceAsColor")
    private fun droupDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.sortbydialog, null)
        val cbAll = view.findViewById<CheckBox>(R.id.cbAll)
        val cbSystem = view.findViewById<CheckBox>(R.id.cbSystem)
        val cbManual = view.findViewById<CheckBox>(R.id.cbManual)
        val cbNew = view.findViewById<CheckBox>(R.id.cbNew)
        val btnApply = view.findViewById<AppCompatButton>(R.id.btnApply)
        val ivClose = view.findViewById<AppCompatImageView>(R.id.ivClose)
        cbAll.isChecked = filterList.contains("all")
        cbSystem.isChecked = filterList.contains("system")
        cbManual.isChecked = filterList.contains("manual")
        cbNew.isChecked = filterList.contains("new")
        ivClose.setOnClickListener {
            dialog.dismiss()
        }

        fun updateAllVisibility() {
            val anySelected = cbSystem.isChecked || cbManual.isChecked || cbNew.isChecked
            if (!anySelected) {
                cbAll.isChecked = true
                if (!filterList.contains("all")) filterList.add("all")
            } else {
                cbAll.isChecked = false
                filterList.remove("all")
            }
            filterList.remove("all")
        }

        cbAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                if (!filterList.contains("all")) {
                    cbSystem.isChecked = false
                    cbManual.isChecked = false
                    cbNew.isChecked = false
                    filterList.add("all")
                    filterList.remove("system")
                    filterList.remove("manual")
                    filterList.remove("new")
                }

            }
        }
        cbSystem.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                filterList.add("system")

            } else {
                filterList.remove("system")
            }
            updateAllVisibility()
        }
        cbManual.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                filterList.add("manual")
            } else {
                filterList.remove("manual")
            }
            updateAllVisibility()
        }
        cbNew.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                filterList.add("new")
            } else {
                filterList.remove("new")
            }
            updateAllVisibility()
        }

        btnApply.setOnClickListener {
            dialog.dismiss()
            getClientList()
        }
        dialog.setContentView(view)
        val sheet = dialog.findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet)
        sheet?.setBackgroundColor(Color.TRANSPARENT)
        dialog.show()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListener() {
        binding.txtClearFilter.setOnClickListener {
            clearFilter()
        }

        binding.relNew.setOnClickListener {
            changeStatusValue(true,false,false,false)
            resetBackgrounds()
            handleSelectedUI(binding.relNew,binding.tvCount,binding.tvNew,binding.tvCount,"new")
        }

        binding.relToday.setOnClickListener {
            changeStatusValue(false,true,false,false)
            resetBackgrounds()
            handleSelectedUI(binding.relToday,binding.tvTodayCount,binding.tvToday,binding.tvTodayCount,"today")
        }


        binding.relCurrentMonth.setOnClickListener {
            changeStatusValue(false,false,true,false)
            resetBackgrounds()
            handleSelectedUI(binding.relCurrentMonth,binding.tvCurrentMonthCount,binding.tvCurrentMonth,binding.tvCurrentMonthCount,"current_,month")
        }

        binding.relLastMonth.setOnClickListener {
            changeStatusValue(false,false,false,true)
            resetBackgrounds()
            handleSelectedUI(binding.relLastMonth,binding.tvLastMonthCount,binding.tvLastMonth,binding.tvLastMonthCount,"last_month")
        }

        binding.relStatus.setOnClickListener {
            resetBackgrounds()
            openBottomSheetLead(
                1,
                leadListDropDown as ArrayList<Any>,
                binding.txtStatus,
                "Select Lead Stage"
            )
        }



        binding.relLeads.setOnClickListener {
            resetBackgrounds()
            openBottomSheetAllLead()
        }

        binding.backButton.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }

        model.clientAdapter.setOnclickListener {
            it as Data
            if (it.is_pending_system_lead == "0") {
                addFragment(requireActivity(), AddLeadStatus(), bundleOf("leadId" to it.lead_id))
            } else {
                (requireActivity() as MainActivity).openBottomSheetAlert()
            }
        }
    }

    private fun handleSelectedUI(
        relNew: RelativeLayout,
        count: TextView,
        tvValue: TextView,
        countColor: TextView,
        value: String
    ) {

        relNew.setBackgroundResource(R.drawable.bg_toggle_selected)
        count.setBackgroundResource(R.drawable.text_background_selected)
        tvValue.setTextColor(Color.parseColor("#8000D8"))
        countColor.setTextColor(Color.WHITE)
        selectedSingleItem=value
        getClientList()
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
            if (viewType == 1) {
                it as LeadStageData
                changeStatusValue(false,false,false,false)
                selectedStatusItem=it.name
                txtLeadStatus.text = it.name
                txtLeadStatus.tag = it.id
                handleStatusColor()
                getClientList()
            }
        }

        close?.setOnClickListener {
            dialog.dismiss()
        }


    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun openBottomSheetAllLead(
    ) {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomSheet)
        dialog.setContentView(R.layout.leads_filter)
        dialog.show()
        val close = dialog.findViewById<ImageView>(R.id.ivClose)
        val txtAllLeads = dialog.findViewById<TextView>(R.id.txtAllLeads)
        val txtSystemLeads = dialog.findViewById<TextView>(R.id.txtSystemLeads)
        val txtManualLeads = dialog.findViewById<TextView>(R.id.txtManualLeads)

        txtAllLeads?.setOnClickListener {
            changeStatusValue(false,false,false,false)
            selectedLeadsItem="all"
            binding.txtLeadStatus.text=txtAllLeads.text.toString()
            dialog.dismiss()
            getClientList()
        }

        txtSystemLeads?.setOnClickListener {
            changeStatusValue(false,false,false,false)
            selectedLeadsItem="system"
            binding.txtLeadStatus.text=txtSystemLeads.text.toString()
            dialog.dismiss()
            getClientList()
        }

        txtManualLeads?.setOnClickListener {
            changeStatusValue(false,false,false,false)
            selectedLeadsItem="manual"
            binding.txtLeadStatus.text=txtManualLeads.text.toString()
            dialog.dismiss()
            getClientList()
        }


        close?.setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun handleStatusColor() {
        binding.relStatus.setBackgroundResource(R.drawable.bg_toggle_selected)
        binding.txtStatus.setTextColor(Color.parseColor("#8000D8"))
        binding.ivDropStatus.setBackgroundResource(R.drawable.ic_arrow_down)
    }


    private fun getClientList() {
        val param = JsonObject()

//        val selectedFilter = JsonArray()
//        for (item in filterList) {
//            selectedFilter.add(item)
//        }
        param.addProperty("lead_type", selectedLeadsItem)
        param.addProperty("status", selectedStatusItem)
//        param.addProperty("time_filter", selectedSingleItem)
        model.getClientListFilter(param) {
            if (it.status == 0) {
                binding.tvMessage.text="Yay! You have got ${it.total_count} Leads"
                binding.tvCount.text=it.new_count.toString()
                binding.tvTodayCount.text=it.today_count.toString()
                binding.tvCurrentMonthCount.text=it.current_month_count.toString()
                binding.tvLastMonthCount.text=it.last_month_count.toString()
                if (it.data.all.isEmpty() && it.data.new.isEmpty() && it.data.today.isEmpty() && it.data.current_month.isEmpty() && it.data.last_month.isEmpty()) {
                    binding.txtNoDataFound.visibility = View.VISIBLE
                    binding.recClient.visibility = View.GONE
                } else {
                    binding.txtNoDataFound.visibility = View.GONE
                    binding.recClient.visibility = View.VISIBLE
                    if (isNew){
                        leadList=it.data.new
                        Log.d("fdassdffd", "getClientList1: $leadList")
                        hideVisibleNoData(leadList)
                    }else if (isToday){
                        leadList=it.data.today
                        hideVisibleNoData(leadList)
                    }
                    else if (isLastMonth){
                        leadList=it.data.last_month
                        hideVisibleNoData(leadList)
                    }else if (isCurrentMonth){
                        leadList=it.data.current_month
                        hideVisibleNoData(leadList)
                    }else{
                        leadList = it.data.all
                        hideVisibleNoData(leadList)
                    }
                    model.clientAdapter.updateAdapter(leadList as ArrayList<Any>)
                }
            }
        }
    }

    private fun hideVisibleNoData(leadList: ArrayList<Data>) {
        if (leadList.isEmpty()){
            binding.txtNoDataFound.visibility = View.VISIBLE
            binding.recClient.visibility = View.GONE
        }else{
            binding.txtNoDataFound.visibility = View.GONE
            binding.recClient.visibility = View.VISIBLE
        }
    }

    private fun changeStatusValue(new: Boolean, today: Boolean, currentMonth: Boolean, lastMonth: Boolean) {
        isNew=new
        isToday=today
        isCurrentMonth=currentMonth
        isLastMonth=lastMonth
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
            binding.swipeRefreshLayout.isRefreshing = it
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
            (activity as MainActivity).setHeader2("", STATUS_COLOR1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}


