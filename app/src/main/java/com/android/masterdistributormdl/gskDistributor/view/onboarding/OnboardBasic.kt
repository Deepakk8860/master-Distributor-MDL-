package com.android.masterdistributormdl.gskDistributor.view.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
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


class OnboardBasic : Fragment() {
    private lateinit var binding: OnboardBasicBinding
    lateinit var model: OnboardModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.onboard_basic, container, false)
        model = ViewModelProvider(this)[OnboardModel::class.java]
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
            (requireActivity() as OnboardActivity).setHeader("Onboard")

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
            AlertError.show(requireContext(), it) {}
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireContext(), it)
        }
        setData()
        setClearError(binding.nameLay, binding.etName)
        setClearError(binding.mobileLay, binding.etMobile)
        setClearError(binding.mobile2Lay, binding.etMobile2)
        setClearError(binding.emailLay, binding.etEmail)
        setClearError(binding.email2Lay, binding.etEmail2)
        setClearError(binding.gstLay, binding.etGst)
        setClearError(binding.etLicNameLay, binding.etLicName)

        (requireActivity() as OnboardActivity).keyboard(binding.mainLayout, binding.next)
        binding.next.setOnClickListener { check() }
    }

    fun setData() {
        binding.etMobile.isEnabled = false
        binding.etEmail.isEnabled = false
        val user = model.getUser()
        binding.etName.setText(user.fullname)
        binding.etMobile.setText(user.mobile)
        binding.etMobile2.setText(user.alt_mobile)
        binding.etEmail.setText(user.email)
        binding.etEmail2.setText(user.alt_email)
        binding.etGst.setText(user.gstin)
        binding.etLicName.setText(user.company)
        (requireActivity() as OnboardActivity).setNavigation(user.kyc_status!!, 1,user.video)
    }

    fun check() {
        if (binding.etName.text.toString().isEmpty()) {
            binding.nameLay.requestFocus()
            binding.nameLay.error = " "
        } else if (!checkMobileNo(binding.etMobile.text.toString())) {
            binding.mobileLay.requestFocus()
            binding.mobileLay.error = " "
        } else if (!checkMobileNo(binding.etMobile2.text.toString()) && binding.etMobile2.length() != 0) {
            binding.mobile2Lay.requestFocus()
            binding.mobile2Lay.error = " "
        } else if (!isEmailValid(binding.etEmail.text.toString())) {
            binding.emailLay.requestFocus()
            binding.emailLay.error = " "
        } else if (!isEmailValid(binding.etEmail2.text.toString()) && binding.etEmail2.length() != 0) {
            binding.email2Lay.requestFocus()
            binding.email2Lay.error = " "
        } else if (!checkGstinNo(binding.etGst.text.toString()) && binding.etGst.length() != 0) {
            binding.gstLay.requestFocus()
            binding.gstLay.error = " "
        } else if (binding.etLicName.text.toString().isEmpty()) {
            binding.etLicNameLay.requestFocus()
            binding.etLicNameLay.error = " "
        } else {
            updateBasic()
        }
    }

    private fun updateBasic() {
        val param = JsonObject()
        param.addProperty("name", binding.etName.text.toString())
        param.addProperty("alt_mobile", binding.etMobile2.text.toString())
        param.addProperty("alt_email", binding.etEmail2.text.toString())
        param.addProperty("gstin", binding.etGst.text.toString())
        param.addProperty("company", binding.etLicName.text.toString())
        model.update_basic(param) {
            if (it.status == 0) {
                getUserProfile()
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }

    }

    fun getUserProfile() {
        model.getUserProfile {
            if (it.status == 0) {
                model.save(it.data)
                replaceFragment(requireActivity(), OnboardAddress())
            }
            else if (it.status==100){
                //handle maintenance
            }
            else {
                AlertError.show(requireActivity(), it.message) {}
            }
        }
    }

}





