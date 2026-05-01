package com.android.masterdistributormdl.addLead


import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.android.masterdistributormdl.adapter.HomeAdapter
import com.android.masterdistributormdl.addLead.AddLeadStatus
import com.android.masterdistributormdl.databinding.FollowUpLeadListBinding
import com.android.masterdistributormdl.databinding.FollowUpLeadsBinding
import com.android.masterdistributormdl.databinding.LeadStatusBinding
import com.android.masterdistributormdl.databinding.ManageClientBinding
import com.android.masterdistributormdl.home.Home

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
import com.android.masterdistributormdl.utils.loadImage
import com.android.masterdistributormdl.utils.replaceFragment
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject


class NewLeadList : Fragment() {
    private lateinit var binding: FollowUpLeadListBinding
    lateinit var model: HomeModel
    private var type=""
    private var count=""

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
        binding.txtTitle.text= getString(R.string.new_lead_list)
    }

    private fun initListener() {
        binding.backButton.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }

        model.followLeadAdapter.setOnclickListener {
            it as Data
            addFragment(requireActivity(), AddLeadStatus(), bundleOf("leadId" to it.lead_id))
        }
    }


    private fun getAllFollowUpClientList() {
        val param = JsonObject()
        param.addProperty("type", type)
        model.getFollowUpList(param) {
            if (it.status == 0) {
                binding.recAllClient.addItemDecoration(
                    DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
                )
                model.followLeadAdapter.updateAdapter(it.data as ArrayList<Any>)
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
            (activity as MainActivity).setHeader2("", STATUS_COLOR1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}


