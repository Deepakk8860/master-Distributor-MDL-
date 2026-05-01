package com.android.masterdistributormdl.profile


import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.BuildConfig
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ProfileBinding

import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.model.profile.Data
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.addFragment
import com.android.masterdistributormdl.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.utils.loadImageWithCoil
import com.android.masterdistributormdl.utils.onBackResult
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.JsonObject

class Profile : Fragment() {
    private lateinit var binding: ProfileBinding
    lateinit var model: ProfileModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.profile, container, false)
        model = ViewModelProvider(this)[ProfileModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        handleRetrofitMessage()
        initView()
        initListener()
        getProfile()
    }

    private fun initListener() {
        binding.llLogout.setOnClickListener {
            // Post logout event
            // Post logout event to all subscribers

            (requireActivity() as MainActivity).logoutDialog()
        }

        binding.backButton.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }

        binding.relEditProfile.setOnClickListener {
            addFragment(requireActivity(), EditProfile())
        }
    }

    private fun initView() {

        onBackResult("editProfile", requireActivity()) {
            getProfile()
        }


    }

    private fun getProfile() {
        val param = JsonObject()
        model.getProfile(param) {
            if (it.status == 0) {
                setData(it.data)
            }
        }
    }

    private fun setData(it: Data) {
        loadImageWithCoil(binding.image,it.profilephoto)
        binding.txtFullName.setText(it.fullname)
        binding.mobileNumber.setText(it.mobile)
        binding.whatsappNumber.setText(it.whatsapp)
        binding.emailAddress.setText(it.email)
        binding.edtCompanyName.setText(it.company)
        binding.edtPincode.setText(it.home_pincode)

        if (BuildConfig.DEBUG) {
            binding.txtVersion.text = "VERSION Staging : " + BuildConfig.VERSION_NAME
        } else {
            binding.txtVersion.text = "VERSION : " + BuildConfig.VERSION_NAME
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


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setHeader()
        }
    }


    private fun setHeader() {
        try {
            shooterFragment = this
            (activity as MainActivity).setHeader2("", STATUS_COLOR1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}


