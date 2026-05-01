package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.SettlementsBinding
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.gsk.distributor.model.ErrorAlert
import com.gsk.distributor.model.SettlementItem
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Settlements : Fragment() {
    private lateinit var binding: SettlementsBinding
    lateinit var model: SettlementsModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.settlements, container, false)
        model = ViewModelProvider(this)[SettlementsModel::class.java]
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

        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else {
                InternetError.show(requireContext())
            }
        }
        binding.back.setOnClickListener { requireActivity().onBackPressed() }
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it
            Loading.showHide2(requireContext(), it)
        }
        binding.swipeRefreshLayout.setOnRefreshListener { model.settlementreport() }
        model.settlementreport()
    }


}


class SettlementsModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val user = sharedPreference.getUser()!!
    val earnsAdapter = HomeAdapter(this, 20)

    init {
        val json = sharedPreference.getString("Settlements")

        if (json!!.isNotEmpty()) {
            val data: ArrayList<SettlementItem> =
                gson.fromJson(json, object : TypeToken<ArrayList<SettlementItem>>() {}.type)
            earnsAdapter.updateAdapter(data as ArrayList<Any>)
        }

    }


    fun settlementreport() {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().settlementreport(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                var data = ArrayList<SettlementItem>()
                if (it.status == 0) {
                    data = it.data
                } else {
                    errorMessage.value = it.message
                }
                sharedPreference.putString("Settlements", gson.toJson(data))
                earnsAdapter.updateAdapter(data as ArrayList<Any>)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }


}


