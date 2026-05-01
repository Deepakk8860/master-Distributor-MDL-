package com.android.masterdistributormdl.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.adapter.LeadAdapter
import com.android.masterdistributormdl.addLead.AddLeadStatus
import com.android.masterdistributormdl.addLead.CreatePaymentLink
import com.android.masterdistributormdl.addLead.ManageLead
import com.android.masterdistributormdl.addLead.PreferedMessageSharing
import com.android.masterdistributormdl.databinding.HomeBinding
import com.android.masterdistributormdl.doc.PdfPreview
import com.android.masterdistributormdl.follow_up.FollowUpAllList
import com.android.masterdistributormdl.follow_up.FollowUpLead
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.model.TaskItem
import com.android.masterdistributormdl.model.lead.Data
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.addFragment
import com.android.masterdistributormdl.utils.replaceFragment
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject


class Home : Fragment() {
    private lateinit var binding: HomeBinding
    lateinit var model: HomeModel
    private val leadAdapter = LeadAdapter(1)
    private var countSystem=""
    private var countManual=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.home, container, false)
        model = ViewModelProvider(this)[HomeModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        initListener()
        handleRetrofitMessage()
        handleLeadData()
        getFollowUpListCount()
        getLeadSummary("7d")
        getFollowUpList()
    }

    private fun getFollowUpListCount() {
        val param = JsonObject()
        param.addProperty("type","home")
        model.getFollowUpListCount(param) {
            if (it.status == 0) {
                countSystem=it.data.system
                countManual=it.data.manual
                binding.headerTitle.text = "TODAY'S FOLLOW UPS(${it.data.today})"
                binding.txtTotalCount.text = it.data.system_total
                binding.txtNewLeadCount.text =it.data.system
                binding.txtManuallyCount.text =it.data.manual
                binding.txtTotalManualCount.text = it.data.manual_total
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

    private fun getFollowUpList() {
        val param = JsonObject()
        param.addProperty("type", "today")
        model.getFollowUpList(param) {
            if (it.status == 0) {
                if (it.data.isNotEmpty()) {
                    binding.llFollowUpListData.visibility = View.VISIBLE
                    binding.llFollowUpLeads.visibility = View.GONE
                    binding.recFollowUpLeads.addItemDecoration(
                        DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
                    )
                    // Show only 2 items initially, if available
                    val previewList = if (it.data.size >= 2) {
                        it.data.take(2)
                    } else {
                        it.data // will be 0 or 1 item
                    }
                    model.followLeadAdapter.updateAdapter(ArrayList(previewList))
                } else {
                    binding.llFollowUpLeads.visibility = View.VISIBLE
                    binding.llFollowUpListData.visibility = View.GONE
                }
            }
        }
    }

    private fun getLeadSummary(dayValue: String) {
        val param = JsonObject()
        param.addProperty("range", dayValue)
        model.getLeadSummary(param) {
            if (it.status == 0) {
                binding.leadsAssigned.text = it.data.leads_assigned
                binding.leadsContacted.text = it.data.leads_contacted
                binding.totalNewLeadsCount.text = it.data.new_leads
                binding.responseTime.text = it.data.response_time
            }
        }
    }

    private fun handleLeadData() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            getFollowUpListCount()
            getFollowUpList()
            getLeadSummary("7d")
        }
// Initialize RecyclerView
        binding.recLead.layoutManager = LinearLayoutManager(requireContext())

        // Sample task data
        val taskList = arrayListOf(
            TaskItem("CONNECT A LEAD SOURCE"),
            TaskItem("ADD/IMPORT 5 CONTACTS"),
            TaskItem("SEND 5 QUICK RESPONSES"),
            TaskItem("CREATE A PAGE"),
            TaskItem("SHARE PAGES 3 TIMES"),
            TaskItem("CREATE A LEAD FORM")
        )

        // Set the adapter
        leadAdapter.updateAdapter(taskList as ArrayList<Any>)
        binding.recLead.adapter = leadAdapter
    }

    private fun initListener() {
        binding.llTotalLead.setOnClickListener {
            (requireActivity() as MainActivity).updateBottom(4,ManageLead())
        }


        binding.relDistributorApp.setOnClickListener {
            val intent = Intent(requireActivity(), com.android.masterdistributormdl.gskDistributor.view.MainActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.slide_down)
        }

        binding.relNewLead.setOnClickListener {
            addFragment(requireActivity(), FollowUpAllList(), bundleOf("type" to "system_new","count" to countSystem))
        }

        binding.relManualList.setOnClickListener {
            addFragment(requireActivity(), FollowUpAllList(), bundleOf("type" to "manual_new","count" to countManual))
        }

        binding.ivDayFilter.setOnClickListener {
            openBottomSheet(binding.ivDayFilter)
        }


        binding.relFollowUpListData.setOnClickListener {
            (requireActivity() as MainActivity).updateBottom(3,FollowUpLead())
        }

        binding.seeAllFollowUps.setOnClickListener {
            (requireActivity() as MainActivity).updateBottom(3,FollowUpLead())
        }

        model.followLeadAdapter.setOnclickListener {
            it as Data
            addFragment(requireActivity(), AddLeadStatus(), bundleOf("leadId" to it.lead_id))
        }
    }


    private fun openBottomSheet(ivDayFilter: TextView) {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomSheet)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_date_range, null)
        val tvPast7Days = view.findViewById<TextView>(R.id.tvPast7Days)
        val tvPast24Hours = view.findViewById<TextView>(R.id.tvPast24Hours)
        val isPast7Days = ivDayFilter.text.toString() == "Past 7 days"
        val selectedColor = Color.parseColor("#01579B")
        val defaultColor = Color.parseColor("#000000")

        tvPast7Days.setTextColor(if (isPast7Days) selectedColor else defaultColor)
        tvPast24Hours.setTextColor(if (isPast7Days) defaultColor else selectedColor)

        tvPast7Days.setOnClickListener {
            dialog.dismiss()
            ivDayFilter.text = getString(R.string.past_7_days)
            getLeadSummary("7d")
        }

        tvPast24Hours.setOnClickListener {
            dialog.dismiss()
            ivDayFilter.text = getString(R.string.past_24_hours)
            getLeadSummary("24h")
        }

        dialog.setContentView(view)
        dialog.show()

        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            dialog.dismiss()
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


