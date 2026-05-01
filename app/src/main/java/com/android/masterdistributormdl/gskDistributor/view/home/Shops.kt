package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ShopsBinding
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.gsk.distributor.model.ErrorAlert
import com.gsk.distributor.model.Item
import com.gsk.distributor.model.ShopItem
import com.gsk.distributor.model.ShopsResult
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
import java.util.Calendar
import java.util.concurrent.ThreadLocalRandom


class Shops : Fragment() {
    lateinit var model: ShopsModel
    private lateinit var binding: ShopsBinding
    val itemArray = ArrayList<Item>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.shops, container, false)
        model = ViewModelProvider(this)[ShopsModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    fun int() {
        itemArray.clear()
        itemArray.add(Item("ALL", "All"))
        itemArray.add(Item("ACTIVE", "Active"))
        itemArray.add(Item("DEACTIVE", "Deactivate"))

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
        model.type = requireArguments().getString("type")!!
        model.agent_id = requireArguments().getString("agent_id")
        setHeader()
        int()
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
        model.isDialogVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireActivity(), it)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            getShops()
        }



        binding.etStartDate.setOnClickListener {
            startDatePicker()
        }
        binding.etEndDate.setOnClickListener {
//            droupDialogFilter()
//            endDatePicker()
        }

        binding.search.setOnClickListener {
            getShops()
        }
        binding.edtSearch.addTextChangedListener {
            if (binding.edtSearch.text!!.isNotEmpty()){
                model.shopAdapter.searchFilter(binding.edtSearch.text.toString())
                if (model.shopAdapter.arrayList.size>0){
                    binding.txtNoDataFound.visibility=View.GONE
                    binding.downloadReport.visibility=View.VISIBLE
                }else{
                    binding.txtNoDataFound.visibility=View.VISIBLE
                    binding.downloadReport.visibility=View.GONE
                }

            }else{
                model.shopAdapter.clearSearch()
                binding.txtNoDataFound.visibility=View.GONE
                binding.downloadReport.visibility=View.VISIBLE
            }
        }
//        binding.downloadReport.visibility = View.GONE

        binding.spinner.visibility = View.INVISIBLE

        model.shopAdapter.setOnclickListener {
            it as ShopItem
            if (it.countCustomer){
                addFragment(requireActivity(), ReportDownloads(), bundleOf("reference" to "count"))
            }else{
//                addFragment(requireActivity(), ViewCertificateRetailer(), bundleOf("shopId" to it.id))

            }
//            downloadShopCerticate(it as ShopItem)
        }
        binding.title.text = requireArguments().getString("title")
        binding.downloadReport.setOnClickListener {
            startDownloading()
        }
        binding.count.text = ""
        binding.etStartDate.setText(printDataFormat(model.startDate))
        binding.etEndDate.setText(printDataFormat(model.endDate))
        setSpinner()
        getShops()
        binding.spinnerClick.setOnClickListener {
            binding.spinner.performClick()
        }
    }

    fun getShops() {
        model.getShops {
            if (it.status == 0) {
                Log.d("dfghfgufugh", "getShops: ${model.type}")
                binding.txtNoDataFound.visibility=View.GONE
                binding.edtSearch.visibility=View.VISIBLE
                if (model.type == "report") {
                    model.shopAdapter.viewType = 6
                    binding.downloadReport.visibility = View.VISIBLE
                } else if (model.type == "report_sale") {
                    model.shopAdapter.viewType = 23
                    binding.downloadReport.visibility = View.VISIBLE
                }
                binding.count.text = "${it.data.size} Retailer"
                model.shopAdapter.updateAdapter1(it.data as ArrayList<Any>)
            } else {
//                binding.count.text = "0 Retailer"
                binding.downloadReport.visibility = View.GONE
                model.shopAdapter.updateAdapter1(ArrayList())
                if (it.message=="No data found."){
                    binding.edtSearch.visibility=View.GONE
                    binding.txtNoDataFound.visibility=View.VISIBLE
                }

//                showToastShort(it.message)
            }

        }
    }


    private fun setSpinner() {
        val list = itemArray.map { it.name }
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, list
        )
        binding.spinner.adapter = adapter
        adapter.setDropDownViewResource(R.layout.spin_dropdown)
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                model.selected = itemArray[position].id
                getShops()
                binding.seletedDown.text = list[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

    }


    private fun startDownloading() {
        val dowloadUrl = BASE_URL + "/$URL_PATH_START/$URL_PATH/retailerdetail"
        val fileName = model.getFileName()
        val param = model.getReq()
        param.addProperty("type", "download")
        val downloader = DownloadFIle()
        downloader.download(requireContext(), dowloadUrl, param, fileName)
    }

    private fun downloadShopCerticate(it: ShopItem) {
//        model.shopCerticate(it.id) { url ->
//            if (url.isNullOrEmpty()) {
//                showToastShort("certificate not created")
//            } else {
//                val downloader = DownloadFIle()
//                downloader.download(requireContext(), url)
//            }
//        }
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

class ShopsModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val isDialogVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
    var agent_id: String? = null;
    val shopAdapter = HomeAdapter(this, 3)
    var startDate = ""
    var endDate = ""

    lateinit var type: String
    lateinit var selected: String

    init {
        val calender = Calendar.getInstance()
        endDate = calenderToDate(calender)
        calender.add(Calendar.MONTH, -1)
        startDate = calenderToDate(calender)
    }

//    fun shopCerticate(shopid: String, url: (String?) -> Unit) {
//        isDialogVisible.value = true
//        val param = JsonObject()
//        param.addProperty("uid", sharedPreference.getString(user_id))
//        param.addProperty("shopid", shopid)
//        viewModelScope.launch {
//            kotlin.runCatching {
//                withContext(Dispatchers.IO) {
//                    getClient().generateshopCerticate(param).body()!!
//                }
//            }.onSuccess {
//                isDialogVisible.value = false
//                val it = JsonObj(it)
//                if (it.getInt("status") == 0) {
//                    url.invoke(it.getString("uri"))
//                } else {
//                    showToastShort(it.getString("message"))
//                }
//            }.onFailure {
//                isDialogVisible.value = false
//                retrofitError.postValue(errorRetrofit(it))
//            }
//        }
//    }

    fun getReq(): JsonObject {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
//        param.addProperty("status", selected)
        param.addProperty("status", "ALL")
        param.addProperty("agentId", agent_id)
        param.addProperty("date", "$startDate - $endDate")
        return param
    }

    fun getFileName(): String {
        val random = ThreadLocalRandom.current().nextInt(1000, 9999)
        return "FranchiesReport_{$random}_${printDataFormat(startDate)} to ${printDataFormat(endDate)}$file_ext"
    }

    fun getShops(result: (ShopsResult) -> Unit) {
        isLoaderVisible.value = true
        val param = getReq()
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getShops(param).body()!!
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







