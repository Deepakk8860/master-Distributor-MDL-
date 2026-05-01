package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ProfileInfoBinding
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.indiaDate
import com.android.masterdistributormdl.gskDistributor.utils.loadImage
import com.android.masterdistributormdl.gskDistributor.utils.printDataFormat
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.gsk.distributor.model.ErrorAlert

import com.android.masterdistributormdl.gskDistributor.view.MainActivity


class ProfileInfo : Fragment() {
    lateinit var model: ProfileModel
    private lateinit var  binding: ProfileInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.profile_info, container, false)
        model = ViewModelProvider(this)[ProfileModel::class.java]
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
            (activity as MainActivity).setHeader("", STATUS_COLOR2)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()

        binding.back.setOnClickListener { requireActivity().onBackPressed() }

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
        setData()

    }

    fun setData() {
        val user = model.user
        loadImage(binding.image, user.profilephoto, R.drawable.logo)
        binding.name.text = user.fullname
        binding.branchId.text  =user.branch_id
        binding.email.text = user.email
        if (user.alt_email.isEmpty()) {
            binding.altEmailLay.visibility = View.GONE
        }
        binding.altEmail.text = user.alt_email
        binding.mobile.text =  user.mobile
        if (user.alt_mobile.isEmpty()) {
            binding.altMobileLay.visibility = View.GONE
        }

        // Check if the list is empty
        if (user.territory.isEmpty()) {
            binding.homeTerritory.text = "NA"
        } else {
            // Assuming you have a UserData object called `userData`
            val pincodeList = user.territory.map { it.pincode }
            val pincodeString = pincodeList.joinToString(",")
            binding.homeTerritory.text = pincodeString
        }
        binding.altMobile.text =  user.alt_mobile
        binding.gender.text = user.gender
        binding.license.text = user.company
        binding.dob.text = printDataFormat(user.dob)
        binding.rDate.text = indiaDate(user.create_dt)
        binding.officeAdd.text = user.ofc_address
        binding.homeAdd.text = user.home_address

    }

}

class ProfileModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val user = sharedPreference.getUserDist()!!


}



