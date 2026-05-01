package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.NotificationBinding
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.gsk.distributor.model.PushItem
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_WHITE
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.utils.SharedPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Notification : Fragment() {
    lateinit var model: NotiModel
    private lateinit var  binding: NotificationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.notification, container, false)
        model = ViewModelProvider(this)[NotiModel::class.java]
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
            (activity as MainActivity).setHeader("", STATUS_WHITE)
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
        model.isDialogVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireActivity(), it)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {

            model.pushnotification()
        }
        model.pushnotification()
        model.notiAdapter.setOnclickListener {
            openNext(it as PushItem)
        }
    }

    fun openNext(it: PushItem) {
        val type = it.type
        val id = it.type_id
        if (type == "TICKET") {
            val bundle = bundleOf("ticket_id" to id, "data" to null)
            addFragment(requireActivity(), TicketInfo(), bundle)
        }else {
          //  addFragment(requireActivity(), NotiInfo(), bundleOf("data" to it))
        }

    }

}

class NotiModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val isDialogVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
    var notiArray = ArrayList<PushItem>()
    val notiAdapter = HomeAdapter(this, 9)
    val MESSAGE = "MESSAGE"

    init {
        val json = sharedPreference.getString(MESSAGE)
        if (json!!.isNotEmpty()) {
            notiArray = gson.fromJson(json, object : TypeToken<ArrayList<PushItem>>() {}.type)
        }
        notiAdapter.updateAdapter(notiArray as ArrayList<Any>)
    }

    fun pushnotification() {
        val param = JsonObject()
        param.addProperty("uid", user.id)
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().pushnotification(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                notiArray.clear()
                if (it.status == 0) {
                    notiArray = it.data
                } else {
                    errorMessage.value = it.message
                }
                notiAdapter.updateAdapter(notiArray as ArrayList<Any>)
                sharedPreference.putString(MESSAGE, gson.toJson(notiArray))

            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }


    fun pushNotificationRead(id: String) {
        val param = JsonObject()
        param.addProperty("uid", user.id)
        param.addProperty("notification_id", id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().pushNotificationRead(param).body()!!
                }
            }.onSuccess {
                if (it.status == 0) {
                }
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

}







