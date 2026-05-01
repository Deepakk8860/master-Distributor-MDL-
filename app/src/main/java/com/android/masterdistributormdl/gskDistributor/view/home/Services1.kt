package com.android.masterdistributormdl.gskDistributor.view.home


import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.masterdistributormdl.utils.SharedPreference
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.Services1Binding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.ServicesItem
import com.gsk.distributor.model.ServicesItem2
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.SuccessAlert
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.utils.getPriceFormat
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToast
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


class Services1 : Fragment() {
    lateinit var model: Services1Model
    private lateinit var binding: Services1Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.services1, container, false)
        model = ViewModelProvider(this)[Services1Model::class.java]
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
        model.setViewType(1)
        setHeader()
        val type = requireArguments().getString("type")!!
        val title = requireArguments().getString("title")!!
        binding.title.text = title
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
            AlertError.show(requireContext(), it) { requireActivity().onBackPressed() }
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it
            Loading.showHide2(requireActivity(), it)
        }
        model.isDialogVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireActivity(), it)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            model.getServicesMulti(type)
        }
        model.getServicesMulti(type)


        model.services1Adapter.setOnclickListener {

            bottomSheetDialog(it as ServicesItem2)

        }

    }


    @Keep
    data class ComiData(var customer: Double = 0.0, var shop: Double = 0.0)

    fun setEditPrice(
        editText1: TextInputEditText, editText2: TextInputEditText,

        result: (ComiData) -> Unit
    ) {
        val data = ComiData(0.0)
        editText1.addTextChangedListener {
            var amount = 0.0
            if (editText1.text.toString().isNotEmpty()) {
                amount = editText1.text.toString().toDouble()

                editText1.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._14sp)
                )
            } else {
                editText1.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._13sp)
                )
            }
            data.customer = amount
            result.invoke(data)
        }
        editText2.addTextChangedListener {
            var amount = 0.0
            if (editText2.text.toString().isNotEmpty()) {
                amount = editText2.text.toString().toDouble()
                editText2.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._14sp)
                )
            } else {
                editText2.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._13sp)
                )
            }
            data.shop = amount
            result.invoke(data)
        }
    }

    private fun hideKeyboard() {
        Loading.show2(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(500))
            withContext(Dispatchers.Main) {
                Loading.dismiss()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bottomSheetDialog(item: ServicesItem2) {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.service_edit_price)

        val bottomSheet =
            dialog.findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        dialog.setOnDismissListener {
            hideKeyboard()
        }
        val close = dialog.findViewById<ImageView>(R.id.close)!!
        val inputLay1 = dialog.findViewById<TextInputLayout>(R.id.inputLay1)!!
        val etPrice1 = dialog.findViewById<TextInputEditText>(R.id.etPrice1)!!
        val inputLay2 = dialog.findViewById<TextInputLayout>(R.id.inputLay2)!!
        val etPrice2 = dialog.findViewById<TextInputEditText>(R.id.etPrice2)!!
        val comiPrice = dialog.findViewById<TextView>(R.id.comiPrice)!!

        val customerMin = dialog.findViewById<TextView>(R.id.customerMin)!!
        val customerMax = dialog.findViewById<TextView>(R.id.customerMax)!!
        val shopMin = dialog.findViewById<TextView>(R.id.shopMin)!!
        val shopMax = dialog.findViewById<TextView>(R.id.shopMax)!!
        val customer_min = item.min
        val customer_max = item.max
        val shop_min = item.min_shop_commission
        val shop_max = item.max_shop_commission
        customerMin.text = "Min " + getPriceFormat(customer_min)
        customerMax.text = "Max " + getPriceFormat(customer_max)
        shopMin.text = "Min " + getPriceFormat(shop_min)
        shopMax.text = "Max " + getPriceFormat(shop_max)

        val save = dialog.findViewById<Button>(R.id.save)!!
        dialog.show()


        close.setOnClickListener {
            hideKeyboard()
            dialog.dismiss()
        }
        var comi = 0.0
        var recon = 1.0
        setEditPrice(etPrice1, etPrice2) {
            comi = 0.0
            recon = 1.0
            val customer = it.customer
            val shop_comi = it.shop
            if (customer > customer_max) {
                val str = etPrice1.text.toString().dropLast(1)
                etPrice1.setText(str)
                etPrice1.setSelection(etPrice1.length())
                return@setEditPrice
            } else if (shop_comi > shop_max) {
                val str = etPrice2.text.toString().dropLast(1)
                etPrice2.setText(str)
                etPrice2.setSelection(etPrice2.length())
                return@setEditPrice
            }
            if ((customer in customer_min..customer_max) && (shop_comi in shop_min..shop_max)) {
                val remain_amt = (customer - item.mrp - shop_comi)
                comi = (remain_amt * item.commission_value) / 100
                recon = customer - item.mrp - shop_comi - ((remain_amt * 20) / 100) - comi
            }
            comiPrice.text = getPriceFormat(comi)
            Log.d(TAG + "-Commission", "Comi : $comi || Recon : " + recon)
        }
        if (item.customer_price.toInt() > 0) etPrice1.setText(
            item.customer_price.toInt().toString()
        )

        if (item.shop_commission.toInt() > 0) etPrice2.setText(
            item.shop_commission.toInt().toString()
        )

        save.setOnClickListener {
            if (comi > 0 && recon == 0.0) {
                val param = JsonObject()
                param.addProperty("productid", item.id)
                param.addProperty("customer_price", etPrice1.text.toString())
                param.addProperty("shop_commission", etPrice2.text.toString())
                model.updatePrice(param) {
                    if (it.status == 0) {
                        dialog.dismiss()
                        SuccessAlert.show(requireContext(), it.message) {
                            model.services1Adapter.selected = -1
                            model.getServices()
                        }
                    } else {
                        showToastShort(it.message)
                    }
                }
            } else {
                showToast("Please enter correct Customer Price or Retailer Commission amount")
            }
        }
    }
}


class Services1Model(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val isDialogVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
    private var type = 0
    val services1Adapter = HomeAdapter(this, 10)

    var arrayList = ArrayList<ServicesItem>()

    fun setViewType(type: Int) {
        this.type = type
        val json = sharedPreference.getString("services$type")
        try {
            if (json!!.isNotEmpty()) {
                arrayList =
                    gson.fromJson(json, object : TypeToken<ArrayList<ServicesItem>>() {}.type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (type == 1) {
            services1Adapter.viewType = 10
            setViewType1()
        } else if (type == 2) {
            services1Adapter.viewType = 11
            setViewType2()
        } else if (type == 3) {
            services1Adapter.viewType = 12
            setViewType1()
        } else if (type == 4) {
            services1Adapter.viewType = 24
            setViewType2()
        } else if (type == 5) {
            services1Adapter.viewType = 26
            setViewType2()
        }
    }


    fun getServices() {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("type", type)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getServices(param).body()!!
                }
            }.onSuccess {
                arrayList.clear()
                isLoaderVisible.value = false
                if (it.status == 0) {
                    arrayList.addAll(it.data)
                } else {
                    errorMessage.value = it.message
                }
                sharedPreference.putString("services$type", gson.toJson(arrayList))
                Log.d(TAG, "getServices: $type")
                if (type == 1) {
                    setViewType1()
                } else if (type == 2) {
                    setViewType2()
                } else if (type == 3) {
                    setViewType1()
                } else if (type == 4) {
                    setViewType2()
                } else if (type == 5) {
                    setViewType2()
                }
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun getServicesMulti(typeUnit: String) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("type", typeUnit)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getServices(param).body()!!
                }
            }.onSuccess {
                arrayList.clear()
                isLoaderVisible.value = false
                if (it.status == 0) {
                    arrayList.addAll(it.data)
                } else {
                    errorMessage.value = it.message
                }
                sharedPreference.putString(
                    "services${this@Services1Model.type}",
                    gson.toJson(arrayList)
                )
                if (this@Services1Model.type == 1) {
                    setViewType1()
                } else if (this@Services1Model.type == 2) {
                    setViewType2()
                } else if (this@Services1Model.type == 3) {
                    setViewType1()
                } else if (this@Services1Model.type == 4) {
                    setViewType2()
                } else if (this@Services1Model.type == 5) {
                    setViewType2()
                }
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    private fun setViewType1() {
        if (arrayList.size > 0) {
            services1Adapter.updateAdapter(arrayList[0].data as ArrayList<Any>)
        } else {
            services1Adapter.updateAdapter(ArrayList())
        }
    }

    private fun setViewType2() {
        services1Adapter.updateAdapter(arrayList as ArrayList<Any>)
    }

    fun updatePrice(param: JsonObject, result: (ApiResponse) -> Unit) {
        isDialogVisible.value = true

        param.addProperty("uid", sharedPreference.getString(user_id))

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().updatePrice(param).body()!!
                }
            }.onSuccess {
                isDialogVisible.value = false

                result(it)

            }.onFailure {
                isDialogVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

}




