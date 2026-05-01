package com.android.masterdistributormdl.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.masterdistributormdl.BuildConfig
import com.android.masterdistributormdl.databinding.SplashActivityBinding
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.utils.NetworkAlert
import com.android.masterdistributormdl.utils.SharedPreference
import com.android.masterdistributormdl.utils.showToastShort
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: SplashActivityBinding
    var utm_source: String = ""
    val sharedPreference = SharedPreference()
    val user = sharedPreference.getUser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= SplashActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        initView()
        handleRetrofitMessage()
        initListener()

        // val user = sharedPreference.getUser()

        NetworkAlert.init(this)
        next()
    }

    private fun initView(){
        Log.d("hfgbhggbhg", "setFragment: login3")
        if (BuildConfig.DEBUG) {
            binding.versionName.text = "VERSION staging : " + BuildConfig.VERSION_NAME
        } else {
            binding.versionName.text = "VERSION : " + BuildConfig.VERSION_NAME
        }
    }


    private fun handleRetrofitMessage(){

    }


    private fun initListener(){
        binding.versionName.setOnClickListener {
            showToastShort(utm_source)
        }
    }


    private fun next() {
        /* val uri = intent.data
         var segment = ""
         if (uri != null) {
             Log.d(TAG, "DeepLink " + uri.toString())
             val parameters = uri.getPathSegments()
             if (parameters.size > 0) {
                 segment = parameters.get(0)
                 Log.d(TAG, "DeepLink2 " + segment)
             }
         }
         Log.d(TAG + "-utm_source", "" + utm_source)*/
        openNext()
    }

    fun openNext() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(1500))
            withContext(Dispatchers.Main) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


}

