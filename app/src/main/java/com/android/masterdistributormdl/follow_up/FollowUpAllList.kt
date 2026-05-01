package com.android.masterdistributormdl.follow_up


import android.annotation.SuppressLint
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.addLead.AddLeadStatus
import com.android.masterdistributormdl.databinding.FollowUpLeadListBinding

import com.android.masterdistributormdl.home.HomeModel
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.model.lead.Data
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.addFragment
import com.android.masterdistributormdl.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.gson.JsonObject


class FollowUpAllList : Fragment() {
    private lateinit var binding: FollowUpLeadListBinding
    lateinit var model: HomeModel
    private var type = ""
    private var count = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.follow_up_lead_list, container, false)
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
        initListener()
        getAllFollowUpClientList()
    }

    private fun initView() {
        type = requireArguments().getString("type") ?: ""
        count = requireArguments().getString("count") ?: ""
        setTitle(type, count)

        binding.swipeRefreshLayout.setOnRefreshListener {
            getAllFollowUpClientList()
        }
    }

    private fun setTitle(type: String, count: String) {
        when (type) {
            "overdue" -> setValue(count,"Overdue")
            "upcoming" ->setValue(count,"Upcoming")
            "someday" ->setValue(count,"Someday")
            "system_new" ->setValue(count,"System New")
            "manual_new" ->setValue(count,"Manual New")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setValue(count: String, title: String) {
        binding.txtTitle.text ="$title($count)"
    }

    private fun initListener() {
        binding.backButton.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }

        model.followLeadAdapter.setOnclickListener {
            it as Data
            if (it.is_pending_system_lead == "0") {
                addFragment(requireActivity(), AddLeadStatus(), bundleOf("leadId" to it.lead_id))
            } else {
                (requireActivity() as MainActivity).openBottomSheetAlert()
            }
        }
    }


    private fun getAllFollowUpClientList() {
        val param = JsonObject()
        param.addProperty("type", type)
        model.getFollowUpList(param) {
            if (it.status == 0) {
                if (it.data.isEmpty()){
                    binding.txtNoDataFound.text=it.message
                    binding.txtNoDataFound.visibility=View.VISIBLE
                    binding.recAllClient.visibility=View.GONE
                }else{
                    binding.txtNoDataFound.visibility=View.GONE
                    binding.recAllClient.visibility=View.VISIBLE
                    binding.recAllClient.addItemDecoration(
                        DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
                    )
                    model.followLeadAdapter.updateAdapter(it.data as ArrayList<Any>)
                }
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


