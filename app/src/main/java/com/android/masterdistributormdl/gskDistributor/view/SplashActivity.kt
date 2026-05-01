package com.android.masterdistributormdl.gskDistributor.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.masterdistributormdl.BuildConfig
import com.android.masterdistributormdl.databinding.SplashActivityBinding
import com.android.masterdistributormdl.utils.SharedPreference

import com.pushwoosh.Pushwoosh.PUSH_RECEIVE_EVENT
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: SplashActivityBinding
    val sharedPreference = SharedPreference()
    val user = sharedPreference.getUser()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        if (BuildConfig.DEBUG) {
            binding.versionName.text = "VERSION staging : " + BuildConfig.VERSION_NAME
        } else {
            binding.versionName.text = "VERSION : " + BuildConfig.VERSION_NAME
        }
        binding.versionName.setOnClickListener {
            //   setData()
        }
        val data = intent.getStringExtra(PUSH_RECEIVE_EVENT)
        next(data)
    }

    fun openDirect(data: String?) {

    }


    private fun next(data: String?) {
        val time = if (data.isNullOrEmpty())  200 else 3500
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(time.toLong()))
            withContext(Dispatchers.Main) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.putExtra(PUSH_RECEIVE_EVENT, data)
                startActivity(intent)
                finish()
            }
        }
    }
}

