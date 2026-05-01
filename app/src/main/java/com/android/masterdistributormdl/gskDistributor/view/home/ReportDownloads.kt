package com.android.masterdistributormdl.gskDistributor.view.home


import android.app.Application
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ReportDownloadBinding
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.gsk.distributor.model.ReportDownLoadResult
import com.gsk.distributor.model.ReportDownlaodItem
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.android.masterdistributormdl.gskDistributor.utils.BASE_URL
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.utils.URL_PATH
import com.android.masterdistributormdl.gskDistributor.utils.URL_PATH_START
import com.android.masterdistributormdl.gskDistributor.utils.calenderToDate
import com.android.masterdistributormdl.gskDistributor.utils.file_ext_csv
import com.android.masterdistributormdl.gskDistributor.utils.getDateFormat
import com.android.masterdistributormdl.gskDistributor.utils.printDataFormat
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.utils.SharedPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.ThreadLocalRandom


class ReportDownloads : Fragment() {
    lateinit var model: ReportDownloadModel
    private lateinit var binding: ReportDownloadBinding
    private var reference=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.report_download, container, false)
        model = ViewModelProvider(this)[ReportDownloadModel::class.java]
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
        reference=requireArguments().getString("reference")!!

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
            model.downloadsArray.clear()
            if (reference=="count"){
                getReportDownloadsWithoutFilter()
            }else{
                getReportDownloads()
            }

        }
        if (reference=="count"){
            binding.cornerLayout.visibility=View.GONE
            getReportDownloadsWithoutFilter()

        }else{
            getReportDownloads()
        }



        binding.etStartDate.setOnClickListener {
            startDatePicker()
        }
        binding.etEndDate.setOnClickListener {
            endDatePicker()
        }
        binding.search.visibility = View.VISIBLE
        binding.search.setOnClickListener {
            getReportDownloads()
        }

        binding.edtSearch.addTextChangedListener {

            if (binding.edtSearch.text!!.isNotEmpty()) {

                model.reportDownloadAdapter.searchFilter(binding.edtSearch.text.toString())
                if (model.reportDownloadAdapter.arrayList.size > 0) {
                    binding.txtNoDataFound.visibility = View.GONE
                    binding.downloadReport.visibility = View.VISIBLE
                } else {
                    binding.txtNoDataFound.visibility = View.VISIBLE
                    binding.downloadReport.visibility = View.GONE
                }
                Log.d(TAG, "onViewCreated: working ${model.reportDownloadAdapter.arrayList.size}")

            } else {
                model.reportDownloadAdapter.clearSearch()
                binding.txtNoDataFound.visibility = View.GONE
                binding.downloadReport.visibility = View.VISIBLE
            }
        }
        binding.downloadReport.setOnClickListener {
            startDownloading()
        }
        binding.etStartDate.setText(printDataFormat(model.startDate))
        binding.etEndDate.setText(printDataFormat(model.endDate))
        binding.detailsLay.visibility = View.GONE
        binding.downloadReport.visibility = View.GONE
    }


    private fun getReportDownloads() {
        model.getReportDownloads {
            if (it.status == 0) {
                binding.txtNoDataFound.visibility = View.GONE
                binding.edtSearch.visibility = View.VISIBLE
                binding.count.text = "${it.data.size} Customer"
//                binding.detailsLay.visibility = View.VISIBLE
                binding.downloadReport.visibility = View.VISIBLE
                model.reportDownloadAdapter.updateAdapter1(it.data as ArrayList<Any>)
            } else {
                binding.downloadReport.visibility = View.GONE
                binding.detailsLay.visibility = View.GONE
//                showToastShort(it.message)
                model.reportDownloadAdapter.updateAdapter1(ArrayList())
                if (it.message == "No data found.") {
                    binding.edtSearch.visibility = View.GONE
                    binding.txtNoDataFound.visibility = View.VISIBLE
                }

            }
        }
    }

    private fun getReportDownloadsWithoutFilter() {
        model.getReportDownloadsWithoutFilter {
            if (it.status == 0) {
                binding.txtNoDataFound.visibility = View.GONE
                binding.edtSearch.visibility = View.VISIBLE
                binding.count.text = "${it.data.size} Customer"
//                binding.detailsLay.visibility = View.VISIBLE
                binding.downloadReport.visibility = View.VISIBLE
                model.reportDownloadAdapter.updateAdapter1(it.data as ArrayList<Any>)
            } else {
                binding.downloadReport.visibility = View.GONE
                binding.detailsLay.visibility = View.GONE
//                showToastShort(it.message)
                model.reportDownloadAdapter.updateAdapter1(ArrayList())
                if (it.message == "No data found.") {
                    binding.edtSearch.visibility = View.GONE
                    binding.txtNoDataFound.visibility = View.VISIBLE
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

    private fun startDownloading() {
        val dowloadUrl = BASE_URL + "/$URL_PATH_START/$URL_PATH/customerdetail"
        val fileName = model.getFileName()
        val param = model.getReq()
        param.addProperty("type", "download")
//        param.addProperty("type", "csv")
        val downloader = DownloadFIle()
        Log.d(TAG, "startDownloading: $dowloadUrl")
        downloader.download(requireContext(), dowloadUrl, param, fileName)
    }


}

class ReportDownloadModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
    val downloadsArray = ArrayList<ReportDownlaodItem>()
    val detailsLay = MutableLiveData<Int>(View.GONE)
    val itemCount = MutableLiveData<String>()
    val reportDownloadAdapter = HomeAdapter(this, 7)
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
        val random = ThreadLocalRandom.current().nextInt(1000, 9999)
        return "CustomerReport_${random}_${printDataFormat(startDate)} to ${printDataFormat(endDate)}$file_ext_csv"
    }

    fun getReportDownloads(result: (ReportDownLoadResult) -> Unit) {
        downloadsArray.clear()
        isLoaderVisible.value = true
        val param = getReq()

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getReportDownloads(param).body()!!
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

    fun getReportDownloadsWithoutFilter(result: (ReportDownLoadResult) -> Unit) {
        downloadsArray.clear()
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getReportDownloads(param).body()!!
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





