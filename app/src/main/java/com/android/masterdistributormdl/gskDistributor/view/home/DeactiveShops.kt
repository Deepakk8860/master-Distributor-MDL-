package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.PendingShopBinding
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.gsk.distributor.model.ShopsResult
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DeactiveShops : Fragment() {
    lateinit var model: DeShopsModel
    private lateinit var binding: PendingShopBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.pending_shop, container, false)
        model = ViewModelProvider(this)[DeShopsModel::class.java]
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
        binding.title.text = requireArguments().getString("title")
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
        binding.detailsLay.visibility = View.GONE

        getShops()
        model.shopAdapter.setOnclickListener {

        }


    }

    fun getShops() {
        val type = requireArguments().getString("type")
        val end: String
        if (type.equals("pending")) {
            end = "deactiverequestshop"
            binding.detail.text = "Deactivation Pending Details"
        } else if (type.equals("agreement")) {
            binding.detail.text = "Pending Agreement Details"
            end = "pendingagreement"
        } else {
            requireActivity().onBackPressed()
            showToastShort("Invalid Page")
            return
        }
        model.getShops(end) {
            if (it.status == 0) {
                binding.detailsLay.visibility = View.VISIBLE
                binding.count.text = "${it.data.size} Retailer"
                model.shopAdapter.updateAdapter(it.data as ArrayList<Any>)
            } else {
                binding.count.text = "0 Retailer"
                binding.detailsLay.visibility = View.GONE
                model.shopAdapter.updateAdapter(ArrayList())
                showToastShort(it.message)
            }

        }
    }


}

class DeShopsModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val isDialogVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
   val shopAdapter = HomeAdapter(this, 16)


    fun getShops(end: String, result: (ShopsResult) -> Unit) {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
    viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().deactiverequestshop(end, param).body()!!
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







