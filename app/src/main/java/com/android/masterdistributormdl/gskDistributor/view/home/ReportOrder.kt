package com.android.masterdistributormdl.gskDistributor.view.home

import android.annotation.SuppressLint
import android.app.Application
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ReportOrderBinding
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.gsk.distributor.model.ReportOrderResult
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.BASE_URL
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.utils.URL_PATH
import com.android.masterdistributormdl.gskDistributor.utils.URL_PATH_START
import com.android.masterdistributormdl.gskDistributor.utils.calenderToDate
import com.android.masterdistributormdl.gskDistributor.utils.file_ext
import com.android.masterdistributormdl.gskDistributor.utils.getDateFormat
import com.android.masterdistributormdl.gskDistributor.utils.printDataFormat
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.ThreadLocalRandom


class ReportOrder : Fragment() {
    lateinit var model: ReportOrderModel
    private lateinit var binding: ReportOrderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.report_order, container, false)
        model = ViewModelProvider(this)[ReportOrderModel::class.java]
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

        binding.edtSearch.addTextChangedListener {
            if (binding.edtSearch.text!!.isNotEmpty()){
                model.reportOrderAdapter.searchFilter(binding.edtSearch.text.toString())
                if (model.reportOrderAdapter.arrayList.size>0){
                    binding.txtNoDataFound.visibility=View.GONE
                    binding.downloadReport.visibility=View.VISIBLE
                }else{
                    binding.txtNoDataFound.visibility=View.VISIBLE
                    binding.downloadReport.visibility=View.GONE
                }
                Log.d(TAG, "onViewCreated: size${model.reportOrderAdapter.arrayList.size}")

            }else{
                model.reportOrderAdapter.clearSearch()
                binding.txtNoDataFound.visibility=View.GONE
                binding.downloadReport.visibility=View.VISIBLE
            }
        }

        reportOrder()

        binding.etStartDate.setOnClickListener {
            startDatePicker()
        }
        binding.etEndDate.setOnClickListener {
            endDatePicker()
        }
        binding.search.visibility = View.VISIBLE
        binding.search.setOnClickListener {
            reportOrder()
        }
        binding.downloadReport.setOnClickListener {
            startDownloading()
        }
        binding.etStartDate.setText(printDataFormat(model.startDate))
        binding.etEndDate.setText(printDataFormat(model.endDate))
        binding.detailsLay.visibility = View.GONE
        binding.downloadReport.visibility = View.GONE
    }

    private fun startDownloading() {
        val dowloadUrl = BASE_URL + "/$URL_PATH_START/$URL_PATH/orderdetail"
        val fileName = model.getFileName()
        val param = model.getReq()
        param.addProperty("type", "download")
        val downloader = DownloadFIle()
        downloader.download(requireContext(), dowloadUrl, param, fileName)
    }

    @SuppressLint("SetTextI18n")
    fun reportOrder() {
        model.reportOrder {
            if (it.status == 0) {
                binding.txtNoDataFound.visibility=View.GONE
                binding.edtSearch.visibility=View.VISIBLE
                binding.count.text = it.data.size.toString() + " Orders"
//                binding.detailsLay.visibility = View.VISIBLE
                binding.downloadReport.visibility = View.VISIBLE
                model.reportOrderAdapter.updateAdapter1(it.data as ArrayList<Any>)
            } else {
                binding.detailsLay.visibility = View.GONE
                binding.downloadReport.visibility = View.GONE
                if (it.message=="No data found."){
                    binding.edtSearch.visibility=View.GONE
                    binding.txtNoDataFound.visibility=View.VISIBLE
                }
//                showToastShort(it.message)
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
}


class ReportOrderModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
    val reportOrderAdapter = HomeAdapter(this, 5)
    var startDate = ""
    var endDate = ""

    init {
        val calender = Calendar.getInstance()
        endDate = calenderToDate(calender)
        calender.add(Calendar.MONTH, -1)
        startDate = calenderToDate(calender)
    }

    fun getReq(): JsonObject {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("date", "$startDate - $endDate")
        return param
    }

    fun getFileName(): String {
        val random = ThreadLocalRandom.current().nextInt(/* origin = */ 1000, /* bound = */ 9999)
        return "OrderReport_${random}_${printDataFormat(startDate)} to ${printDataFormat(endDate)}$file_ext"
    }

    fun reportOrder(result: (ReportOrderResult) -> Unit) {

        isLoaderVisible.value = true
        val param = getReq()

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getReportOrders(param).body()!!
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




