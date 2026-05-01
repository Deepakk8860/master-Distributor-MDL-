package com.android.masterdistributormdl.gskDistributor.view.home


import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.WithdrawBinding
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.DashCount
import com.gsk.distributor.model.DashResult
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.model.User
import com.gsk.distributor.model.UserResult
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.dash_count
import com.android.masterdistributormdl.gskDistributor.utils.getPriceFormat
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_data
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.gskDistributor.view.SplashActivity
import com.android.masterdistributormdl.utils.SharedPreference
import com.gsk.distributor.model.AccountDetailsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Withdraw : Fragment() {
    private lateinit var binding: WithdrawBinding

    lateinit var model: WithdrawModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.withdraw, container, false)
        model = ViewModelProvider(this)[WithdrawModel::class.java]
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

            dashCount()
            getUserProfile()
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
            Loading.showHide2(requireActivity(), it)
            binding.swipeRefreshLayout.isRefreshing = it
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            dashCount()
        }
        binding.back.setOnClickListener {

            requireActivity().onBackPressed()
        }
        binding.kycLay.setOnClickListener {
            addFragment(requireActivity(), KycUpdate())
        }
        binding.settingsLay.setOnClickListener {
            addFragment(requireActivity(), AddBank())
        }
        binding.settlementLay.setOnClickListener {
            addFragment(requireActivity(), Settlements())
        }
        binding.withdrawLay.setOnClickListener {
            addFragment(requireActivity(), WithdrawAmt())
        }
        dashCount()
        binding.comiAmt.text = getPriceFormat(0.0)
        val count = model.getDashCount()
        if (count != null) {
            setDashCount(count)
        }
        setData()
    }

    override fun onResume() {
        super.onResume()
        dashCount()
        getUserProfile()
    }

    fun getUserProfile() {
        model.getUserProfile {

            if (it.status == 0) {
                model.save(it.data)
                setData()
            }
            else if (it.status==100){
                //handle maintenance
            }
            else {
                (requireActivity() as MainActivity).unregisterPushNotifications()
                model.sharedPreference.clearSharedPrefernce()
                showToastShort(it.message)
                val intent = Intent(requireActivity(), com.android.masterdistributormdl.main.SplashActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun dashCount() {
        model.dashCount {
            if (it.status == 0) {
                model.sharedPreference.putString(dash_count, gson.toJson(it.data))
                setDashCount(it.data)
            }
        }
    }

    private fun setDashCount(count: DashCount) {
        binding.comiAmt.text = getPriceFormat(count.Earning)

    }

    fun setData() {
        if (binding.mainLayout == null) return
        val user = model.user
        if (!user.kyc_status?.kyc!!) {
            binding.kycLay.visibility = View.VISIBLE
            binding.settingsLay.visibility = View.GONE
            binding.withdrawLay.visibility = View.GONE
            binding.settlementLay.visibility = View.GONE
        } else if (!user.payout) {
            binding.kycLay.visibility = View.GONE
            binding.settingsLay.visibility = View.VISIBLE
            binding.withdrawLay.visibility = View.GONE
            binding.settlementLay.visibility = View.GONE
        } else {
            binding.kycLay.visibility = View.GONE
            binding.settingsLay.visibility = View.GONE
            binding.withdrawLay.visibility = View.VISIBLE
            binding.settlementLay.visibility = View.VISIBLE
        }
    }

}

class WithdrawModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUserDist()!!

    fun save(user: User) {
        sharedPreference.putString(user_data, gson.toJson(user))
        this.user = user
    }

    fun getDashCount(): DashCount? {
        val json = sharedPreference.getString(dash_count)
        var count: DashCount? = null
        if (json!!.isNotEmpty()) {
            count = gson.fromJson(json, object : TypeToken<DashCount>() {}.type)
        }
        return count
    }

    fun getUserProfile(result: (UserResult) -> Unit) {
        val param = JsonObject()
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getUserProfile(param).body()!!
                }
            }.onSuccess {
                result.invoke(it)
            }.onFailure {

                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun getAccountDetails(result: (AccountDetailsResult) -> Unit) {
        val param=JsonObject()
        isLoaderVisible.value = true
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getAccountDetails(param).body()!!
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

    fun dashCount(result: (DashResult) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().dashCount(param).body()!!
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

    fun withdrawCommission(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().withdrawCommission(param).body()!!
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

    fun payoutcharge(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().payoutcharge(param).body()!!
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



