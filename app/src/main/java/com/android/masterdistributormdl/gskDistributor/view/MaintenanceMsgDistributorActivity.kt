package com.android.masterdistributormdl.gskDistributor.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.masterdistributormdl.databinding.MaintenanceActivityBinding
import com.android.masterdistributormdl.utils.SharedPreference


class MaintenanceMsgDistributorActivity : AppCompatActivity() {
    private lateinit var binding: MaintenanceActivityBinding
    val sharedPreference = SharedPreference()
    val user = sharedPreference.getUser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= MaintenanceActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        binding.button.setOnClickListener {
            openNext()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        openNext()
    }


    private fun openNext() {
        sharedPreference.putBoolean("MAINTENANCE_SHOWN", false)
        val intent = Intent(this , MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreference.putBoolean("MAINTENANCE_SHOWN", false)
    }

}

