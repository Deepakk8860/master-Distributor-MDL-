package com.android.masterdistributormdl.gskDistributor.view.home

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ReportSalesAgentBinding
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.gsk.distributor.model.ErrorAlert
import com.gsk.distributor.model.SalesAgentItem
import com.gsk.distributor.model.SalesAgentResult
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.BASE_URL
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.URL_PATH
import com.android.masterdistributormdl.gskDistributor.utils.URL_PATH_START
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.file_ext
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.ThreadLocalRandom


class ReportSalesAgent : Fragment() {
    lateinit var model: ReportSalesAgentModel
    private lateinit var binding: ReportSalesAgentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.report_sales_agent, container, false)
        model = ViewModelProvider(this).get(ReportSalesAgentModel::class.java)
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
            (activity as MainActivity).setHeader("", STATUS_COLOR2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }
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
            binding.swipeRefreshLayout.isRefreshing = it
            Loading.showHide2(requireActivity(), it)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {

            reportOrder()
        }

        reportOrder()
        model.reportOrderAdapter.setOnclickListener {
            it as SalesAgentItem
            val bundle = bundleOf("type" to "report_sale", "title" to "Franchise", "agent_id" to it.id)
            addFragment(requireActivity(), Shops(),bundle)
        }

        binding.downloadReport.setOnClickListener {
            startDownloading()
        }

        binding.downloadReport.visibility = View.GONE
    }

    fun startDownloading() {
        val dowloadUrl = BASE_URL + "/$URL_PATH_START/$URL_PATH/agent_detail"
        val fileName = model.getFileName()
        val param = model.getReq()
        param.addProperty("type", "download")
        val downloader = DownloadFIle()
        downloader.download(requireContext(), dowloadUrl, param, fileName)
    }

    @SuppressLint("SetTextI18n")
    fun reportOrder() {
        model.salesAgents {
            if (it.status == 0) {
                binding.downloadReport.visibility = View.VISIBLE
                model.reportOrderAdapter.updateAdapter(it.data as ArrayList<Any>)
            } else {

                binding.downloadReport.visibility = View.GONE
                showToastShort(it.message)
            }


        }

    }


}


class ReportSalesAgentModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
    val reportOrderAdapter = HomeAdapter(this, 21)


    fun getReq(): JsonObject {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))

        return param
    }

    fun getFileName(): String {
        val random = ThreadLocalRandom.current().nextInt(/* origin = */ 1000, /* bound = */ 9999)
        return "SalesAgentReport_${random}_$file_ext"
    }

    fun salesAgents(result: (SalesAgentResult) -> Unit) {
        isLoaderVisible.value = true
        val param = getReq()
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().salesAgents(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                result.invoke(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }


}




