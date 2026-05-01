package com.android.masterdistributormdl.login

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.OtpVerifyBinding
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.addFragment
import com.android.masterdistributormdl.utils.getHtmlSpanned
import com.android.masterdistributormdl.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit


class OtpVerify : Fragment() {
    private lateinit var binding: OtpVerifyBinding
    lateinit var model: LoginModel
    lateinit var email: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.otp_verify, container, false)
        model = ViewModelProvider(this)[LoginModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        initView()
        handleRetrofitMessage()
        initListener()
        binding.pinView.setTextColor(Color.parseColor("#25233C"))
        binding.pinView.setTextSize(resources.getDimension(R.dimen._8sp).toInt())
        startTImer()
    }

    private fun initListener() {

        binding.back.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }
        binding.loginButton.setOnClickListener {
            setButtonEnabled(it)
            if (binding.pinView.value.length != 4) {
                showToastShort("Enter 4 Digit OTP")
            } else {
                model.showLoader()
                Handler(Looper.getMainLooper()).postDelayed({
                    verifyOtp()
                }, 2000)

            }
        }

        binding.resend.setOnClickListener {
            try {
                if (binding.pinView.value.isNotEmpty())
                    binding.pinView.value = ""
            } catch (_: Exception) {
            }
            setButtonEnabled(it)
            resendOtp()
        }
    }

    fun showLoader(){
        model.isLoaderVisible.value=true
    }

    private fun resendOtp() {
        val param = JsonObject()
        param.addProperty("username", email)
        model.loginUser(param) {
            if (it.status == 0) {
                startTImer()
            }else{
                AlertError.show(requireContext(),it.message){}
            }
        }
    }

    private fun verifyOtp() {
        val param = JsonObject()
        param.addProperty("username", email)
        param.addProperty("otp", binding.pinView.value)
        model.verifyOTP(param) {
            if (it.status == 0) {
                timer.cancel()
                binding.resend.isEnabled = true
                val data =it.data
                lifecycleScope.launch {
                    model.save(it.data.userid, data.session_id)
                    model.getUserProfile(requireActivity())
                }
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }

    }

    fun setButtonEnabled(view: View) {
        Loading.show2(view.context)
        view.isClickable = false
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(1000))
            withContext(Dispatchers.Main) {
                Loading.dismiss()
                view.isClickable = true
            }
        }
    }

    private fun handleRetrofitMessage() {
        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else if (it.status == 1) {
                InternetError.show(requireContext())
            }
        }
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) AlertError.show(requireContext(), it) {}
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) { it ->
            Loading.showHide(requireActivity(), it)
        }
    }


    private fun initView() {
        email = requireArguments().getString("email")!!
        val msg = "We have sent a verification code to <b><font color='#273B4A'>$email</font></b><br>Enter the code in below boxes"

        binding.codeSentTo.text = Html.fromHtml(msg, Html.FROM_HTML_MODE_LEGACY)

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
            (activity as MainActivity).setHeader("Verify OTP", STATUS_COLOR1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startTImer() {
        binding.resend.isEnabled = false
        timer.start()
    }



    private val timer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val second = millisUntilFinished / 1000
//            Log.d(TAG, "OTP-TIMER " + second)
            binding.resend.text =
                getHtmlSpanned("Resend Code in <u><font color='#34A853'>${second + 1} seconds</font></u>")
        }

        override fun onFinish() {
            binding.resend.text =
                getHtmlSpanned("Didn't receive code? <u><font color='#34A853'>Resend Code</font></u>")
            binding.resend.isEnabled = true
        }
    }


}




