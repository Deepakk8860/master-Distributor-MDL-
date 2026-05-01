package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.TiketsBinding
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.gsk.distributor.model.TicketItem
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.calenderToDate
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


class Tickets : Fragment() {
    private lateinit var binding: TiketsBinding
    lateinit var model: TicketsModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.tikets, container, false)
        model = ViewModelProvider(this)[TicketsModel::class.java]
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
        model.type = requireArguments().getString("type")!!
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
        model.isDialogVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireActivity(), it)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {

            model.getTickets(binding)
        }



        binding.etStartDate.setOnClickListener {
            startDatePicker()
        }
        binding.etEndDate.setOnClickListener {
            endDatePicker()
        }

        binding.search.setOnClickListener {
            model.getTickets(binding)
        }

        model.shopAdapter.setOnclickListener {
            addFragment(requireActivity(), TicketInfo(), bundleOf("data" to it as TicketItem))
        }
        if (model.type.equals("ACTIVE")) {
            binding.title.text = "Active Tickets"
        } else {
            binding.title.text = "Closed Tickets"
        }
        model.getTickets(binding)
        binding.etStartDate.setText(printDataFormat(model.startDate))
        binding.etEndDate.setText(printDataFormat(model.endDate))

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

/*    private fun startDatePicker() {
        val calender = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
    requireContext(), R.style.date_dialog_theme,
            { _, year, month, day ->
                model.startDate = getDateFormat(year, month + 1, day)
                binding.etStartDate.setText(printDataFormat(model.startDate))

            },
            calender.get(Calendar.YEAR),
            calender.get(Calendar.MONTH),
            calender.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = calender.timeInMillis
        datePickerDialog.show()
    }*/

/*
    private fun endDatePicker() {
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
            calenderEnd.get(Calendar.YEAR),
            calenderEnd.get(Calendar.MONTH),
            calenderEnd.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = calenderEnd.timeInMillis
        datePickerDialog.datePicker.minDate = calenderStart.timeInMillis
        datePickerDialog.show()
    }
*/
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

class TicketsModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val isDialogVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val user = sharedPreference.getUser()!!
    var shopsArray = ArrayList<TicketItem>()
    val shopAdapter = HomeAdapter(this, 14)
    var startDate = ""
    var endDate = ""
    var type = ""


    init {
        val calender = Calendar.getInstance()
        endDate = calenderToDate(calender)
        calender.add(Calendar.MONTH, -1)
        startDate = calenderToDate(calender)
    }


    fun getTickets(binding: TiketsBinding) {
        shopsArray.clear()
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("status", type)
        param.addProperty("date", "$startDate - $endDate")
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getTickets(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                if (it.status == 0) {
                    binding.txtNoDataFound.visibility=View.GONE
                    shopsArray.addAll(it.data)
                } else {
                    if (it.message=="No record found"){
                        binding.txtNoDataFound.visibility=View.VISIBLE
                    }

//                    showToastShort(it.message)
                }
                shopAdapter.updateAdapter(shopsArray as ArrayList<Any>)

            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }


}







