package com.android.masterdistributormdl.follow_up

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.collection.emptyLongSet
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.addLead.AddLeadStatus
import com.android.masterdistributormdl.databinding.AddClientBinding
import com.android.masterdistributormdl.databinding.FollowUpLeadsBinding

import com.android.masterdistributormdl.home.HomeModel
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.model.lead.Data
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.addFragment
import com.android.masterdistributormdl.utils.checkMobileNo
import com.android.masterdistributormdl.utils.checkNullorEmpty
import com.android.masterdistributormdl.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.utils.replaceFragment
import com.android.masterdistributormdl.utils.scrollToPosition1
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.android.masterdistributormdl.utils.user_id
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject

class FollowUpLead : Fragment() {
    private lateinit var binding: FollowUpLeadsBinding
    lateinit var model: HomeModel
    private var count=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.follow_up_leads, container, false)
        model = ViewModelProvider(this)[HomeModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        handleRetrofitMessage()
        initView()
        getFollowUpListCount()
        getFollowUpList()
        initListener()
    }

    private fun initView() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            getFollowUpListCount()
            getFollowUpList()
        }
    }

    private fun initListener() {
        binding.relOverDue.setOnClickListener {
            addFragment(requireActivity(), FollowUpAllList(), bundleOf("type" to "overdue","count" to binding.txtOverDueCount.text.toString()))
        }
        binding.relUpcoming.setOnClickListener {
            addFragment(requireActivity(), FollowUpAllList(), bundleOf("type" to "upcoming","count" to binding.txtUpcomingDueCount.text.toString()))
        }
        binding.relSomeDay.setOnClickListener {
            addFragment(requireActivity(), FollowUpAllList(), bundleOf("type" to "someday","count" to binding.txtSomedayCount.text.toString()))
        }


        model.followLeadAdapter.setOnclickListener {
            it as Data
            addFragment(requireActivity(),AddLeadStatus(), bundleOf("leadId" to it.lead_id))
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
            binding.swipeRefreshLayout.isRefreshing = it
            Loading.showHide(requireActivity(), it)
        }
    }


    private fun getFollowUpList() {
        val param = JsonObject()
        param.addProperty("type", "today")
        model.getFollowUpList(param) {
            if (it.status == 0) {
                binding.recFollowUpLeads.addItemDecoration(
                    DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
                )
                model.followLeadAdapter.updateAdapter(it.data as ArrayList<Any>)
            }
        }
    }

    private fun getFollowUpListCount() {
        val param = JsonObject()
        param.addProperty("type","followup")
        model.getFollowUpListCount(param) {
            if (it.status == 0) {
                binding.txtOverDueCount.text=it.data.overdue.toString()
                binding.txtUpcomingDueCount.text=it.data.upcoming.toString()
                binding.txtSomedayCount.text=it.data.someday.toString()
                binding.txtToday.text="TODAY(${it.data.today})"
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
            (activity as MainActivity).setHeader2("", STATUS_COLOR1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}


