package com.android.masterdistributormdl.gskDistributor.view.onboarding

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.BasicDetailsInfoBinding
import com.android.masterdistributormdl.databinding.OnboardBasicBinding
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.checkGstinNo
import com.android.masterdistributormdl.gskDistributor.utils.checkMobileNo
import com.android.masterdistributormdl.gskDistributor.utils.isEmailValid
import com.android.masterdistributormdl.gskDistributor.utils.replaceFragment
import com.android.masterdistributormdl.gskDistributor.utils.setClearError
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.gskDistributor.view.home.Home


class OnboardBasicDetails : Fragment() {
    private lateinit var binding: BasicDetailsInfoBinding
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.basic_details_info, container, false)
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
            (requireActivity() as MainActivity).setHeader("Onboard")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()

        binding.next.setOnClickListener {
            val intent = Intent(requireActivity(), OnboardActivity::class.java)
            onboardLauncher.launch(intent)
        }
        binding.nextHome.setOnClickListener {
            countDownTimer?.cancel()
           replaceFragment(requireActivity(),Home())
        }

    }

    private val onboardLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.ivIcon.setImageResource(R.drawable.tick_done)
                binding.txtDetails.text= getString(R.string.thank_you_for_submitting_your_details)
                binding.next.visibility=View.GONE
                binding.nextHome.visibility=View.VISIBLE
                binding.tvRedirectTimer.visibility=View.VISIBLE
                binding.txtAlert.visibility=View.GONE
                startCountdown()

            } else {
                requireActivity().finish()
            }
        }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.tvRedirectTimer.text = "You will be redirected to the dashboard in ${secondsLeft}s"
            }

            override fun onFinish() {
                replaceFragment(requireActivity(), Home())
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }



}





