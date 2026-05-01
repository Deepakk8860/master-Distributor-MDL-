package com.android.masterdistributormdl.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.masterdistributormdl.databinding.ErrorActivityBinding
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.utils.SharedPreference


class MaintenanceActivity : AppCompatActivity() {
    private lateinit var binding: ErrorActivityBinding

    val sharedPreference = SharedPreference()
    val user = sharedPreference.getUser()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ErrorActivityBinding.inflate(layoutInflater)
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
        val intent = Intent(this , com.android.masterdistributormdl.main.MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}

