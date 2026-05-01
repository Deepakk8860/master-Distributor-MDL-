package com.android.masterdistributormdl.gskDistributor.view.home


import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import com.android.masterdistributormdl.utils.SharedPreference
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ShopBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.android.masterdistributormdl.gskDistributor.distributor.AddDistributor
import com.gsk.distributor.model.ErrorAlert
import com.gsk.distributor.model.QuotaCreateResult
import com.gsk.distributor.model.ShopQuota
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.getAppFragmentManager
import com.android.masterdistributormdl.gskDistributor.utils.getHtmlSpanned
import com.android.masterdistributormdl.gskDistributor.utils.getPriceFormat
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.gskDistributor.view.PayTmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit


class Shop : Fragment() {
    private lateinit var binding: ShopBinding
    lateinit var model: ShopModel
    var shop_price = 0.0
    var gatewayCharge = 0.0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.shop, container, false)
        model = ViewModelProvider(this)[ShopModel::class.java]
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
            (activity as MainActivity).setHeader2("")

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
             Loading.showHide(requireActivity(), it)
        }
        binding.support.setOnClickListener { (requireActivity() as MainActivity).openSupport() }

        binding.addShop.setOnClickListener {
            val unlimited = binding.registeredQuotaLay.visibility == View.VISIBLE
            onBackResult("ShopCount")
            val bundle = bundleOf("unlimited" to unlimited)
            addFragment(requireActivity(), AddShop(), bundle)


//            val unlimited = binding.registeredQuotaLay.visibility == View.VISIBLE
//            onBackResult("ShopCount")
//            val bundle = bundleOf("unlimited" to unlimited)
//            addFragment(requireActivity(), AddDistributor(), bundle)
           /* if (model.isLoaderVisible.value == true) {
                showToastShort("Please wait . . .")
            } else if (model.shopQuota.value!!.available > 0 || unlimited) {

            } else {
                val msg =
                    "Your Available Retailer Quota is zero. Please contact Support to upgrade your plan."
                AlertError.show(requireContext(), msg) {}
            }*/
        }
        binding.registeredQuotaLay.visibility = View.GONE
        binding.quotaInfoLay.visibility = View.GONE
        model.shopQuota.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.availShop.text = "0"
                binding.regShop.text = "0"
                binding.regShop2.text = "0"
                shop_price = 0.0
                gatewayCharge = 0.0
            } else {
                binding.availShop.text = it.available.toString()
                binding.regShop.text = it.registered.toString()
                binding.regShop2.text = it.registered.toString()
                shop_price = it.shop_price
                gatewayCharge = it.gatewayCharge
                if (it.unlimited) {
                    binding.quotaInfoLay.visibility = View.GONE
                    binding.registeredQuotaLay.visibility = View.VISIBLE
                } else {
                    binding.quotaInfoLay.visibility = View.VISIBLE
                    binding.registeredQuotaLay.visibility = View.GONE
                }
            }
        }
        binding.payNow.setOnClickListener {
            addQuptaAlert()
        }
        binding.menu.setOnClickListener { (requireActivity() as MainActivity).openDrawer() }
        binding.notification.setOnClickListener { addFragment(requireActivity(), Notification()) }
        binding.shopDetails.setOnClickListener {
            val bundle = bundleOf("type" to "shop", "title" to "Retailers List")
            addFragment(requireActivity(), Shops(), bundle)
        }
        binding.pendingShop.setOnClickListener {
            val bundle = bundleOf("type" to "pending", "title" to "Deactivation Pending")
            addFragment(requireActivity(), DeactiveShops(), bundle)
        }
        binding.pendingAgreement.setOnClickListener {
            val bundle = bundleOf("type" to "agreement", "title" to "Agreements Pending")
            addFragment(requireActivity(), DeactiveShops(), bundle)
        }
        binding.deleteShop.setOnClickListener {
            onBackResult("ShopCount")
            addFragment(requireActivity(), DeActiveShop())
        }

    }


    private fun setEditTextSize(editText: EditText, size: Int) {
        editText.setTextSize(
            TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._14sp)
        )
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

    private fun getInteger(editText: EditText): Int {
        return try {
            editText.text.toString().toInt()
        } catch (e: Exception) {
            0
        }
    }

    private fun addQuptaAlert() {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.alert_shop_quota)
        val bottomSheet = dialog.findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        dialog.setOnDismissListener {
            hideKeyboard()
        }
        val close = dialog.findViewById<ImageView>(R.id.close)!!
        val amountLay = dialog.findViewById<LinearLayout>(R.id.amountLay)!!
        val call_now=dialog.findViewById<TextView>(R.id.call_now)!!
        val purchase = dialog.findViewById<LinearLayout>(R.id.purchase)!!
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)!!
        val totalPrice = dialog.findViewById<TextView>(R.id.totalPrice)!!
        val startShop = dialog.findViewById<TextView>(R.id.startShop)!!
        val apply = dialog.findViewById<TextView>(R.id.apply)!!
        val shopCount = dialog.findViewById<TextView>(R.id.shopCount)!!
        val shopAmount = dialog.findViewById<TextView>(R.id.shopAmount)!!
        val gstAmount = dialog.findViewById<TextView>(R.id.gstAmount)!!
        val gatewayAmount = dialog.findViewById<TextView>(R.id.gatewayAmount)!!
        val etInputLay = dialog.findViewById<TextInputLayout>(R.id.etInputLay)!!
        val etInput = dialog.findViewById<TextInputEditText>(R.id.etInput)!!
        call_now.text= getHtmlSpanned(" Call <font color='#299856'><b>18005711888</b></font> to purchase Retailers ")
        startShop.text =
            getHtmlSpanned("Starting at <font color='#F86202'><b>${getPriceFormat(shop_price)}</b></font> for <font color='#12BE40'><b>1</b></font> Retailer")


        amountLay.visibility = View.GONE
        var amount = 0.0
        val defaultQuota = ArrayList<String>()
        defaultQuota.add("5")
        defaultQuota.add("10")
        defaultQuota.add("15")
        defaultQuota.add("20")
        defaultQuota.add("25")
        val adapter = HomeAdapter(model, 18)
        adapter.updateAdapter(defaultQuota as ArrayList<Any>)
        recyclerView.adapter = adapter

        fun setAmount() {
            Log.d("egfdhgfhdg", "setAmount: working")
            shopCount.text = etInput.text.toString()
            amountLay.visibility = View.VISIBLE
            amount = shop_price * getInteger(etInput)
            val gst = (amount * 18) / 100
            gstAmount.text = getPriceFormat(gst)
            shopAmount.text = getPriceFormat(amount)
            val totalAmount=amount + gst
            val gatewayCharge=(totalAmount * gatewayCharge) / 100
            gatewayAmount.text= getPriceFormat(gatewayCharge)
            totalPrice.text = getPriceFormat(amount + gst + gatewayCharge)
        }

        if (defaultQuota.isNotEmpty()) {
            val firstItem = defaultQuota[0] // Get the first item
            etInput.setText(firstItem) // Set it in the input field
            adapter.selected = 0 // Select first position in adapter
            adapter.updateAdapter() // Update adapter selection
            setAmount() // Update amount details
        }
        etInput.addTextChangedListener {
            apply.setTextColor(Color.parseColor("#F86202"))
            etInputLay.error = null
            if (it.isNullOrEmpty()) {
                amountLay.visibility = View.GONE
                setEditTextSize(etInput, R.dimen._12sp)
            } else {
                adapter.selected = -1
                adapter.updateAdapter()
                for (i in 0 until defaultQuota.size) {
                    if (defaultQuota[i].equals(etInput.text.toString())) {
                        adapter.selected = i
                        adapter.updateAdapter()
                        setAmount()
                    }else{
                        setAmount()
                    }
                }

                setEditTextSize(etInput, R.dimen._14sp)
            }



        }




        adapter.setOnclickListener {
//            apply.text = "Edit"
//            etInput.isEnabled = false
            etInput.setText(it as String)
            setAmount()
        }
        apply.setOnClickListener {
            if (getInteger(etInput) <= 0) {
                etInputLay.error = " "
            } else if (apply.text == "Edit") {
                hideKeyboard()
                apply.text = "Apply"
                amountLay.visibility = View.GONE
                adapter.selected = -1
                adapter.updateAdapter()
                etInput.isEnabled = true
            } else {
                apply.text = "Edit"
                adapter.updateAdapter()
                etInput.isEnabled = false
                setAmount()
            }
        }

        close.setOnClickListener { dialog.dismiss() }
        purchase.setOnClickListener {
            if (amount > 0) {
                dialog.dismiss()
                val intent = Intent(requireActivity(), PayTmActivity::class.java)
                intent.putExtra("image", (requireActivity() as MainActivity).getBitmap())
                intent.putExtra("qty", etInput.text.toString())
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                razorPayLauncher.launch(intent)
            } else {
                etInputLay.requestFocus()
                etInputLay.error = " "
            }
        }
        dialog.show()

    }

    private val razorPayLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                model.getShopQuota()
            } else {
            }
        }

    override fun onResume() {
        super.onResume()
        model.getShopQuota()
    }

    private fun onBackResult(result_key: String) {
        getAppFragmentManager(requireActivity()).setFragmentResultListener(
            result_key, viewLifecycleOwner
        ) { requestKey, bundle ->
            model.getShopQuota()
        }
    }
}

class ShopModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
    val shopQuota = MutableLiveData<ShopQuota?>()

    init {
        val json = sharedPreference.getString("shopquota")
        if (json!!.isNotEmpty()) {
            shopQuota.value = gson.fromJson(json, object : TypeToken<ShopQuota>() {}.type)
        }

    }

    /*  fun save(quota: ShopQuota?) {
          if (quota == null) {
              sharedPreference.putString("shopquota", "")

          } else {
              sharedPreference.putString("shopquota", gson.toJson(quota))
          }
      }*/

    fun getShopQuota() {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getShopQuota(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                shopQuota.value = it.data
                if (it.status == 0) {
                    sharedPreference.putString("shopquota", gson.toJson(it.data))
                } else {
                    sharedPreference.putString("shopquota", "")
                }
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun initShopToken(result: (QuotaCreateResult) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().initShopToken(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                result(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

}



