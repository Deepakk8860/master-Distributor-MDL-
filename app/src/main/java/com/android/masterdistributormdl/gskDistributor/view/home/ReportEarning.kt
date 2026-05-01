package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ReportEarningBinding
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.android.masterdistributormdl.gskDistributor.model.referral.ReferralEarnResult
import com.gsk.distributor.model.DashCount
import com.gsk.distributor.model.ErrorAlert
import com.gsk.distributor.model.ReportEarnResult
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.BASE_URL
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.URL_PATH
import com.android.masterdistributormdl.gskDistributor.utils.URL_PATH_START
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.calenderToDate
import com.android.masterdistributormdl.gskDistributor.utils.currency
import com.android.masterdistributormdl.gskDistributor.utils.dash_count
import com.android.masterdistributormdl.gskDistributor.utils.file_ext
import com.android.masterdistributormdl.gskDistributor.utils.getDateFormat
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.printDataFormat
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


class ReportEarning : Fragment() {
    lateinit var model: ReportEarningModel
    private lateinit var binding: ReportEarningBinding
    lateinit var type: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.report_earning, container, false)
        model = ViewModelProvider(this)[ReportEarningModel::class.java]
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

    private lateinit var selectedTabView: TextView
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
            setTabView()
        }
        binding.earn.setOnClickListener {
            selectedTabView =binding.earn
            setTabView()
        }

        binding.leadPotential.setOnClickListener {
            selectedTabView =binding.leadPotential
            setTabView()
        }

        binding.referral.setOnClickListener {
            selectedTabView =binding.referral
            setTabView()
        }
        binding.potential.setOnClickListener {
            selectedTabView = binding.potential
            setTabView()
        }
        selectedTabView = binding.referral
        setTabView()
        binding.downloadReport.visibility = View.GONE

        binding.downloadReport.setOnClickListener {
            startDownloading()
        }
        binding.etStartDate.setOnClickListener {
            startDatePicker()
        }

        binding.etEndDate.setOnClickListener {
            endDatePicker()
        }
        binding.search.visibility = View.VISIBLE
        binding.search.setOnClickListener {
            setApi()
        }
        binding.withdraw.setOnClickListener {
            addFragment(requireActivity(), Withdraw())
        }

        binding.edtSearch.addTextChangedListener {

            if (binding.edtSearch.text!!.isNotEmpty()) {
                if (selectedTabView==binding.referral || selectedTabView==binding.leadPotential){
                    model.reportOrderAdapter.viewType=29
                    model.reportOrderAdapter.searchFilter(binding.edtSearch.text.toString())
                }else{
                    model.reportOrderAdapter.viewType=8
                    model.reportOrderAdapter.searchFilter(binding.edtSearch.text.toString())
                }

                if (model.reportOrderAdapter.arrayList.size > 0) {
                    binding.txtNoDataFound.visibility = View.GONE
                    binding.downloadReport.visibility = View.VISIBLE
                } else {
                    binding.txtNoDataFound.visibility = View.VISIBLE
                    binding.downloadReport.visibility = View.GONE
                }

            } else {
                model.reportOrderAdapter.clearSearch()
                binding.txtNoDataFound.visibility = View.GONE
                binding.downloadReport.visibility = View.VISIBLE
            }
        }

        binding.totalEarnings.text = currency + "0"
        val count = model.getDashCount()
        if (count != null) {
            if (!count.isWithdraw) {
                binding.withdraw.visibility = View.GONE
            }
        }
    }

    private fun getReportEarn(type: String) {
        model.getReportEarn(type) {
            if (it.status == 0) {
                binding.txtNoDataFound.visibility=View.GONE
                binding.edtSearch.visibility=View.VISIBLE
                model.reportOrderAdapter.updateAdapter1(it.data as ArrayList<Any>)
                binding.downloadReport.visibility = View.VISIBLE
                binding.totalEarnings.text = currency + it.earning
            } else {
                binding.downloadReport.visibility = View.GONE
                binding.totalEarnings.text = currency + "0"
//                showToastShort(it.message)
                model.reportOrderAdapter.updateAdapter1(ArrayList())
                if (it.message=="No data found."){
                    binding.edtSearch.visibility=View.GONE
                    binding.txtNoDataFound.visibility=View.VISIBLE
                }
            }
        }

    }

    private fun getReferralEarn() {
        model.getReferralEarn{
            if (it.status == 0) {
                binding.txtNoDataFound.visibility=View.GONE
                binding.edtSearch.visibility=View.VISIBLE
                model.reportOrderAdapter.updateAdapter1(it.data as ArrayList<Any>)
                binding.downloadReport.visibility = View.VISIBLE
                binding.totalEarnings.text = currency + it.earning
            } else {
                binding.downloadReport.visibility = View.GONE
                binding.totalEarnings.text = currency + "0"
//                showToastShort(it.message)
                model.reportOrderAdapter.updateAdapter1(ArrayList())
                if (it.message=="No data found."){
                    binding.edtSearch.visibility=View.GONE
                    binding.txtNoDataFound.visibility=View.VISIBLE
                }
            }
        }

    }

    private fun getLeadPotential() {
        model.getLeadPotentialEarn{
            if (it.status == 0) {
                binding.txtNoDataFound.visibility=View.GONE
                binding.edtSearch.visibility=View.VISIBLE
                model.reportOrderAdapter.updateAdapter1(it.data as ArrayList<Any>)
                binding.downloadReport.visibility = View.VISIBLE
                binding.totalEarnings.text = currency + it.earning
            } else {
                binding.downloadReport.visibility = View.GONE
                binding.totalEarnings.text = currency + "0"
//                showToastShort(it.message)
                model.reportOrderAdapter.updateAdapter1(ArrayList())
                if (it.message=="No data found."){
                    binding.edtSearch.visibility=View.GONE
                    binding.txtNoDataFound.visibility=View.VISIBLE
                }
            }
        }

    }


    private fun startDatePicker() {
        val value = model.startDate.split("-")
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.set(Calendar.YEAR, value[0].toInt())
        selectedCalendar.set(Calendar.MONTH, value[1].toInt() - 1)
        selectedCalendar.set(Calendar.DAY_OF_MONTH, value[2].toInt())

        val calender = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(), R.style.date_dialog_theme,
            { _, year, month, day ->
                model.startDate = getDateFormat(year, month + 1, day)
                binding.etStartDate.setText(printDataFormat(model.startDate))

            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = calender.timeInMillis
        datePickerDialog.show()
    }

    private fun endDatePicker() {
        val value2 = model.endDate.split("-")
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.set(Calendar.YEAR, value2[0].toInt())
        selectedCalendar.set(Calendar.MONTH, value2[1].toInt() - 1)
        selectedCalendar.set(Calendar.DAY_OF_MONTH, value2[2].toInt())

        val value = model.startDate.split("-")
        val calenderStart = Calendar.getInstance()
        calenderStart.set(Calendar.YEAR, value[0].toInt())
        calenderStart.set(Calendar.MONTH, value[1].toInt() - 1)
        calenderStart.set(Calendar.DAY_OF_MONTH, value[2].toInt())

        val calenderEnd = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(), R.style.date_dialog_theme,
            { _, year, month, day ->
                model.endDate = getDateFormat(year, month + 1, day)
                binding.etEndDate.setText(printDataFormat(model.endDate))

            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = calenderEnd.timeInMillis
        datePickerDialog.datePicker.minDate = calenderStart.timeInMillis
        datePickerDialog.show()
    }

    private fun setTabView() {
        binding.earn.setBackgroundResource(R.color.trans)
        binding.potential.setBackgroundResource(R.color.trans)
        binding.referral.setBackgroundResource(R.color.trans)
        binding.leadPotential.setBackgroundResource(R.color.trans)
        selectedTabView.setBackgroundResource(R.drawable.tab_selector)

        binding.earn.setTextColor(Color.parseColor("#801D3667"))
        binding.potential.setTextColor(Color.parseColor("#801D3667"))
        binding.referral.setTextColor(Color.parseColor("#801D3667"))
        binding.leadPotential.setTextColor(Color.parseColor("#801D3667"))
        selectedTabView.setTextColor(Color.parseColor("#1D3667"))

        model.init()
        binding.etStartDate.setText(printDataFormat(model.startDate))
        binding.etEndDate.setText(printDataFormat(model.endDate))
        setApi()
    }

    private fun setApi() {
        Log.d("jgjghjghjgnhj", "setApi15: ${model.type}")

        if (selectedTabView == binding.earn) {
            Log.d("jgjghjghjgnhj", "setApi11: $selectedTabView")
            model.type = 1
            getReportEarn("completeorderreport")
        } else if (selectedTabView == binding.potential) {
            Log.d("jgjghjghjgnhj", "setApi12: $selectedTabView")
            model.type = 0
            getReportEarn("processingorderreport")
        }
        else if (selectedTabView == binding.referral) {
            model.type = 2
            Log.d("jgjghjghjgnhj", "setApi13: $selectedTabView")
            getReferralEarn()
        }
        else{
            model.type = 3
            Log.d("jgjghjghjgnhj", "setApi14: $selectedTabView")
            getLeadPotential()
        }
    }

    private fun startDownloading() {

        val name: String
        val urlEnd: String
        if (model.type == 1) {
            Log.d("fdgfdgfg", "startDownloading: 1")
            name = "CompleteOrder"
            urlEnd = "completeorderreport"
        } else if (model.type==0){
            Log.d("fdgfdgfg", "startDownloading: 2")
            name = "ProcessingOrder"
            urlEnd = "processingorderreport"
        }else if (model.type==2){
            Log.d("fdgfdgfg", "startDownloading: 3")
            name = "referral"
            urlEnd = "referral_earning"
        }else{
            Log.d("fdgfdgfg", "startDownloading: 4")
            name = "LeadPotential"
            urlEnd = "referral_earning_potential"
        }
        val dowloadUrl = "$BASE_URL/$URL_PATH_START/$URL_PATH/$urlEnd"
        val fileName = "${name}_${model.getDate()}$file_ext"
        val param = model.getReq()
        param.addProperty("type", "download")
        val downloader = DownloadFIle()
        downloader.download(requireContext(), dowloadUrl, param, fileName)
    }

}

class ReportEarningModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
    var type = 1
    val reportOrderAdapter = HomeAdapter(this, 8)
    var startDate = ""
    var endDate = ""

    fun init() {
        val calender = Calendar.getInstance()
        endDate = calenderToDate(calender)
        calender.add(Calendar.MONTH, -1)
        startDate = calenderToDate(calender)
    }

    fun getDashCount(): DashCount? {
        val json = sharedPreference.getString(dash_count)
        var count: DashCount? = null
        if (json!!.isNotEmpty()) {
            count = gson.fromJson(json, object : TypeToken<DashCount>() {}.type)
        }
        return count
    }

    fun getReq(): JsonObject {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("date", "$startDate - $endDate")
        return param
    }

    fun getDate(): String {
        return "${printDataFormat(startDate)} to ${printDataFormat(endDate)}"
    }

    fun getReportEarn(end: String, result: (ReportEarnResult) -> Unit) {

        isLoaderVisible.value = true
        val param = getReq()
        reportOrderAdapter.viewType=8
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getReportEarn(end, param).body()!!
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

    fun getReferralEarn(result: (ReferralEarnResult) -> Unit) {

        isLoaderVisible.value = true
        val param = getReq()
        reportOrderAdapter.viewType=29
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().referralEarn(param).body()!!
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

    fun getLeadPotentialEarn(result: (ReferralEarnResult) -> Unit) {
        isLoaderVisible.value = true
        val param = getReq()
        reportOrderAdapter.viewType=29
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().referralEarnPotential(param).body()!!
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


