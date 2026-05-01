package com.android.masterdistributormdl.gskDistributor.view

import android.app.Application
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.databinding.PaymentActivityBinding
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.gsk.distributor.model.*
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.JsonObj
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.SuccessAlert
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.utils.SharedPreference
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import com.paytm.pgsdk.TransactionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class PayTmActivity : AppCompatActivity() {
    lateinit var model: PaymentModel
    val sharedPreference = SharedPreference()
    val user = sharedPreference.getUser()
    private lateinit var createData: CreateData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this)[PaymentModel::class.java]
        val binding: PaymentActivityBinding = PaymentActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        val byteArray = intent.getByteArrayExtra("image")

        if (byteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            binding.image.setImageBitmap(bitmap)
        }
        model.isLoaderVisible.observe(this) {
            Loading.showHide(this, it)
        }
        model.retrofitError.observe(this) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else {
                InternetError.show(this)
            }
            onFailed("Internet connection error")
        }
        model.errorMessage.observe(this) {
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        initShopToken()
    }

    private fun initShopToken() {
        val qty = intent.getStringExtra("qty")
        model.initShopToken(qty) {
            if (it.status == 0) {
                createData = it.data
                openPaytm()
            } else {
                AlertError.show(this, it.message) {
                    finish()
                }

            }
        }
    }

    private fun openPaytm() {

        val host =
            if (createData.environment.equals("staging", true)) "https://securegw-stage.paytm.in/"
            else "https://securegw.paytm.in/"

        val paytmOrder = PaytmOrder(
            createData.ref_id,
            createData.mid,
            createData.token,
            createData.amount,
            createData.callbackUrl
        )
        val manager = TransactionManager(paytmOrder, object : PaytmPaymentTransactionCallback {
            override fun onTransactionResponse(bundle: Bundle?) {
                Log.d(TAG, "PAYTM : onTransactionResponse \n" + bundle.toString())
                if (bundle == null) return
                if (!bundle.getString("body").isNullOrEmpty()) {
                    var json = JSONObject(bundle!!.getString("body")!!)
                    json = json.getJSONObject("txnInfo")
                    //  onFailed("Transaction Failed : " + json.getString("RESPMSG"))
                    paymentShopUpdate(json)
                } else {
                    val json = JSONObject()
                    val keys = bundle.keySet()
                    for (key in keys) {
                        json.put(key, bundle.getString(key))
                    }
                    paymentShopUpdate(json)
                    /*  if (json.getString("RESPCODE").equals("01")) {
                          paymentShopUpdate(json)
                      } else {
                          onFailed("Transaction Failed : " + json.getString("RESPMSG"))
                      }*/
                }
            }

            override fun networkNotAvailable() {
                onFailed("Network Not Available")
            }

            override fun onErrorProceed(p0: String?) {
                onFailed("Error Proceed")
            }

            override fun clientAuthenticationFailed(p0: String?) {
                onFailed("Client Authentication Failed")
            }

            override fun someUIErrorOccurred(p0: String?) {
                onFailed("Some UI Error Occurred")
            }

            override fun onErrorLoadingWebPage(p0: Int, p1: String?, p2: String?) {
                onFailed("Error Loading WebPage")
            }

            override fun onBackPressedCancelTransaction() {
                onFailed("Back Pressed Cancel Transaction")
            }

            override fun onTransactionCancel(p0: String?, p1: Bundle?) {
                onFailed("Transaction Cancel ")
            }
        })
        manager.setAppInvokeEnabled(false)
        manager.setShowPaymentUrl(host + "theia/api/v1/showPaymentPage");
        manager.startTransaction(this, 1001)
    }

    fun onFailed(message: String) {
        AlertError.show(this, message) {
            setResult(RESULT_CANCELED, message)
        }
    }

    private fun setResult(status: Int, message: String?) {
        val intent = Intent()
        intent.putExtra("order_id", createData.orderid)
        intent.putExtra("message", message)
        setResult(status, intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && data != null) {
            val response = data.getStringExtra("response")
            val logs =
                "PAYTM : onActivityResult\n" + data.getStringExtra("nativeSdkForMerchantMessage") +
                        "\n-------------Response-------------\n" + response
            Log.d(TAG, logs)

            val json = JSONObject(response)
            /*   if (json.getString("RESPCODE").equals("01")) {
                   //   viewModel.tnxStatus(json.toString())
                   paymentShopUpdate(json)
               } else {
                   val msg = "Transaction Failed : " + json.getString("RESPMSG")
                   //onFailed(msg)
                   paymentShopUpdate(json)
               }*/
            paymentShopUpdate(json)
        } else {
            onFailed("Transaction Cancelled")
        }
    }

    fun paymentShopUpdate(response: JSONObject) {
        Log.d(TAG, "paymentShopUpdate: $response")
        val resJson = JsonObj(response.toString())
        val param = JsonObject()
        param.addProperty("orderid", createData.orderid)
        param.add("response", resJson.getJsonObj())
        model.paymentShopUpdate(param) {
            if (it.status == 0) {
                SuccessAlert.show(this, it.message) {
                    setResult(RESULT_OK, it.message)
                }
            } else {
                onFailed(it.message)
            }
        }
    }

}


class PaymentModel(application: Application) : AndroidViewModel(application) {
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


    fun initShopToken(qty: String?, result: (QuotaCreateResult) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("qty", qty)

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

    fun paymentShopUpdate(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true

        param.addProperty("uid", sharedPreference.getString(user_id))

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().paymentShopUpdate(param).body()!!
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