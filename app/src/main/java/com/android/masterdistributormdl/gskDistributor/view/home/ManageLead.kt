package com.android.masterdistributormdl.gskDistributor.view.home


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
import com.android.masterdistributormdl.databinding.LeadStatusBinding
import com.android.masterdistributormdl.databinding.ManageClientBinding
import com.android.masterdistributormdl.databinding.ManageClientDistBinding
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


class ManageLead : Fragment() {
    private lateinit var binding: ManageClientDistBinding
    lateinit var model: HomeModelLead


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.manage_client_dist, container, false)
        model = ViewModelProvider(this)[HomeModelLead::class.java]
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
        getClientList()
    }

    private fun initView() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            getClientList()
        }
    }

    private fun initListener() {
        binding.backButton.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }

      /*  model.clientAdapter.setOnclickListener {
            it as Data
            if (it.is_pending_system_lead == "0") {
                addFragment(requireActivity(), AddLeadStatus(), bundleOf("leadId" to it.lead_id))
            } else {
                (requireActivity() as MainActivity).openBottomSheetAlert()
            }
        }*/
    }


    private fun getClientList() {
        val param = JsonObject()
        model.getClientList(param) {
            if (it.status == 0) {
                if (it.data.isEmpty()){
                    binding.txtNoDataFound.visibility=View.VISIBLE
                    binding.recClient.visibility=View.GONE
                }else{
                    binding.txtNoDataFound.visibility=View.GONE
                    binding.recClient.visibility=View.VISIBLE
                    binding.recClient.addItemDecoration(
                        DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
                    )
                    model.clientAdapter.updateAdapter(it.data as ArrayList<Any>)
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
            (activity as com.android.masterdistributormdl.gskDistributor.view.MainActivity).setHeader("", STATUS_COLOR1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}


