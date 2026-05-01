package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.NotificationSettingsBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser

import com.android.masterdistributormdl.gskDistributor.adapter.NewAdapter
import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.model.NotificationSettingsModel
import com.android.masterdistributormdl.gskDistributor.model.NotificationType
import com.gsk.distributor.model.ReportDownlaodItem
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.calenderToDate
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id

import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.collections.ArrayList


class NotificationSettings : Fragment() {
    private lateinit var binding: NotificationSettingsBinding
    lateinit var model: NotificationModel
    private val jsonListApi = ArrayList<Map<String, String>>()
    private var jsonString=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.notification_settings, container, false)
        model = ViewModelProvider(this)[NotificationModel::class.java]
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
        initListener()
        loader()
        getNotificationSettings()
    }

    private fun loader() {
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
            getNotificationSettings()
        }
    }

    private fun getNotificationSettings() {
        model.getNotificationSettingsList {
            if (it.status == 0) {
                it.notification_type.forEach { notification ->

                    val jsonObject = mapOf(
                        "id" to (notification.id),
                        "push" to notification.pushnotify,
                        "email" to notification.emailnotify
                    )
                    jsonListApi.add(jsonObject)
                }
                binding.recNotificationSettings.visibility = View.VISIBLE
                model.notificationSettingAdapter.updateAdapter(it.notification_type as ArrayList<Any>)

            } else {
                binding.recNotificationSettings.visibility = View.GONE
                showToastShort(it.message)
                model.notificationSettingAdapter.updateAdapter(ArrayList())
            }
        }



    }




    private fun initListener() {

        // Define an ArrayList to store the JSON objects
        val jsonList = ArrayList<Map<String, String>>()

        // Set the switch state change listener
        model.notificationSettingAdapter.setOnSwitchStateChangeListener { any ->
            val stateMap = any as? Map<String, Any>
            stateMap?.let {
                val item = it["item"] as? NotificationType
                val switch1State = it["switch1State"] as? Boolean ?: false
                val switch2State = it["switch2State"] as? Boolean ?: false

                val switchPush = if (switch1State) "Y" else "N"
                val switchEmail = if (switch2State) "Y" else "N"

                // Create a map representing the JSON object
                val jsonObject = mapOf(
                    "id" to (item?.id ?: ""),
                    "push" to switchPush,
                    "email" to switchEmail
                )

                // Add the map to the ArrayList
                jsonList.add(jsonObject)

                val updatedJsonListApi = jsonListApi.map { originalItem ->
                    val updateItem = jsonList.find { it["id"] == originalItem["id"] }
                    if (updateItem != null) {
                        // Merge the updateItem into the originalItem
                        originalItem.toMutableMap().apply {
                            putAll(updateItem) // Update the push and email fields
                        }
                    } else {
                        originalItem // Keep the original item if no update is found
                    }
                }

                jsonString = Gson().toJson(updatedJsonListApi)

            }
        }



        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.updateSettings.setOnClickListener {
            // Parse the global jsonString to JsonElement
            val jsonElement = JsonParser.parseString(jsonString)

            val param=JsonObject()
            param.add("notifications",jsonElement)
            model.updateNotification(param) {
                if (it.status == 0) {
                    showToastShort(it.message)
                    requireActivity().onBackPressed()
                }
            }
        }


    }





}

class NotificationModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
    val downloadsArray = ArrayList<ReportDownlaodItem>()
    val detailsLay = MutableLiveData<Int>(View.GONE)
    val itemCount = MutableLiveData<String>()
    val notificationSettingAdapter = NewAdapter(this, 3)
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
        param.addProperty("datetime", "$startDate - $endDate")
        return param
    }


    fun getNotificationSettingsList(result: (NotificationSettingsModel) -> Unit) {
        val param=JsonObject()
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getNotificationSettings(param).body()!!
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

    fun updateNotification(param:JsonObject,result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().updateNotificationSettings(param).body()!!
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