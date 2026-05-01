package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ReportBinding
import com.google.gson.reflect.TypeToken

import com.gsk.distributor.model.DashCount
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.model.User
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.dash_count
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.is_sales
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_data
import com.android.masterdistributormdl.gskDistributor.view.MainActivity


class Report : Fragment() {
    lateinit var model: ReportModel
    private lateinit var binding: ReportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.report, container, false)
        model = ViewModelProvider(this).get(ReportModel::class.java)
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
            (activity as MainActivity).setHeader2("")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()

        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else {
                InternetError.show(requireContext())
            }
        }
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {

        }
//        binding.support.setOnClickListener { (requireActivity() as MainActivity).openSupport() }
        binding.support.setOnClickListener { (requireActivity() as MainActivity).openConsultant() }
        binding.txtConsultant.setOnClickListener { (requireActivity() as MainActivity).openConsultant() }
        binding.menu.setOnClickListener { (requireActivity() as MainActivity).openDrawer() }
        binding.notification.setOnClickListener { addFragment(requireActivity(), Notification()) }

        binding.reportOrder.setOnClickListener {
            addFragment(requireActivity(), ReportOrder())
        }

        binding.reportShop.setOnClickListener {
            val bundle = bundleOf("type" to "report", "title" to "Retailer Report")
            addFragment(requireActivity(), Shops(), bundle)
        }

        binding.distributorReport.setOnClickListener {
            val bundle = bundleOf("type" to "report", "title" to "Distributor Report")
            addFragment(requireActivity(), GskDistributor(), bundle)
        }
        binding.reportDownloads.setOnClickListener {
            addFragment(requireActivity(), ReportDownloads(),bundleOf("reference" to "report"))
        }
        binding.reportEarning.setOnClickListener {
            addFragment(requireActivity(), ReportEarning())
        }
        binding.salesAgentReport.setOnClickListener {
            addFragment(requireActivity(), ReportSalesAgent())
        }

        if (is_sales) {
            binding.salesAgentReport.visibility = View.VISIBLE
        } else {
            binding.salesAgentReport.visibility = View.GONE
        }
    }

}


class ReportModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUserDist()!!


    fun save(user: User) {
        this.user = user
        sharedPreference.putString(user_data, gson.toJson(user))

    }

    fun getUser() {
        user = sharedPreference.getUserDist()!!
    }

    fun getDashCount(): DashCount? {
        val json = sharedPreference.getString(dash_count)
        var count: DashCount? = null
        if (json!!.isNotEmpty()) {
            count = gson.fromJson(json, object : TypeToken<DashCount>() {}.type)
        }
        return count
    }

}

