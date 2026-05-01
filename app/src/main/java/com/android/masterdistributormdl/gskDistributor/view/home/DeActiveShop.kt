package com.android.masterdistributormdl.gskDistributor.view.home

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.DeActiveShopBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.ShopItem
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_WHITE
import com.android.masterdistributormdl.gskDistributor.utils.SuccessAlert
import com.android.masterdistributormdl.gskDistributor.utils.getDialog
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.setOnBackResult
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DeActiveShop : Fragment() {
    lateinit var model: DeactiveShopModel
    private lateinit var binding: DeActiveShopBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.de_active_shop, container, false)
        model = ViewModelProvider(this)[DeactiveShopModel::class.java]
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
            hideSoftKeyBoard(requireContext())
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
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            model.shopsArray.clear()
            model.getShopsBySearch(binding.search.text.toString())
        }
        binding.swipeRefreshLayout.isEnabled = false
        binding.search.addTextChangedListener {
            if (binding.search.length() < 3) {
                binding.swipeRefreshLayout.isEnabled = false
                binding.hintLay.visibility = View.VISIBLE
                model.shopsArray.clear()
            } else {
                binding.swipeRefreshLayout.isEnabled = true
                binding.hintLay.visibility = View.GONE
                model.getShopsBySearch(binding.search.text.toString())
            }
        }
        model.shopDeactiveAdapter.setOnclickListener {
            deleteShopAlert(it as ShopItem)
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isEnabled) {
                isEnabled = false
                setOnBackResult(requireActivity(), "ShopCount")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun deleteShopAlert(item: ShopItem) {
        val dialog = getDialog(requireContext(), R.layout.deactivate_shop_alert)
        dialog.setCancelable(false)
        val msg = dialog.findViewById<TextView>(R.id.msg)
        val no = dialog.findViewById<TextView>(R.id.no)
        val yes = dialog.findViewById<TextView>(R.id.yes)
        val reasonInputLay = dialog.findViewById<TextInputLayout>(R.id.reasonInputLay)
        val reason = dialog.findViewById<TextInputEditText>(R.id.reason)
        val countMessage = dialog.findViewById<TextView>(R.id.countMessage)
        no.setOnClickListener {
            hideSoftKeyBoard(reason)
            dialog.dismiss()
        }

        reason.addTextChangedListener {
            reasonInputLay.error = null
            countMessage.text = "${reason.length()}/500"
            if (it.isNullOrEmpty()) {
                reason.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._13sp)
                )
            } else {
                reason.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._14sp)
                )
            }
        }
        fun deleteShop() {
            model.deleteShop(item.id, reason.text.toString()) {
                if (it.status == 0) {
                    SuccessAlert.show(requireContext(), it.message) {
                        //  model.getShopsBySearch(search.text.toString())
                        requireActivity().onBackPressed()
                    }
                } else {
                    AlertError.show(requireContext(), it.message) {}
                }
            }

        }
        yes.setOnClickListener {
            if (reason.text.toString().isEmpty()) {
                reasonInputLay.requestFocus()
                reasonInputLay.error = " "
            } else {
                dialog.dismiss()
                deleteShop()
            }
        }
        dialog.show()

    }


}


class DeactiveShopModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
    var shopsArray = ArrayList<ShopItem>()
    val shopDeactiveAdapter = HomeAdapter(this, 4)


    fun getShopsBySearch(search: String) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("search", search)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getShopsBySearch(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                shopsArray.clear()
                if (it.status == 0) {
                    shopsArray.addAll(it.data)
                } else {
                    showToastShort(it.message)
                }
                shopDeactiveAdapter.updateAdapter(shopsArray as ArrayList<Any>)

            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun deleteShop(id: String, reason: String, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("shopid", id)
        param.addProperty("reason", reason)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().deleteShop(param).body()!!
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



