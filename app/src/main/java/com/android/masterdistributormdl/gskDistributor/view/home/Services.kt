package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ServicesBinding
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.view.MainActivity


class Services : Fragment() {
    lateinit var model: ServicesModel
    private lateinit var binding: ServicesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.services, container, false)
        model = ViewModelProvider(this)[ServicesModel::class.java]
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
            (activity as MainActivity).setHeader2("", STATUS_COLOR2)
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
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {

        }
//        binding.support.setOnClickListener { (requireActivity() as MainActivity).openSupport() }
        binding.support.setOnClickListener { (requireActivity() as MainActivity).openConsultant() }
        binding.txtConsultant.setOnClickListener { (requireActivity() as MainActivity).openConsultant() }
        binding.menu.setOnClickListener {  (requireActivity() as MainActivity).openDrawer()  }
        binding.notification.setOnClickListener { addFragment(requireActivity(), Notification()) }
        binding.setRates.setOnClickListener {
            addFragment(requireActivity(),
                Services1(), bundleOf("type" to "1","title" to "Taxation & Registration"))
        }
        binding.webApp.setOnClickListener {
            addFragment(requireActivity(), Services2(),bundleOf("type" to "2","title" to "Design & Development Services"))
        }
        //registration
        binding.cvGstRegistration.setOnClickListener {
            addFragment(requireActivity(),
                Services2(), bundleOf("type" to "15","title" to "Registration"))
        }


        //itr
        binding.cvItr.setOnClickListener {
            addFragment(requireActivity(),
                Services1(), bundleOf("type" to "8","title" to "ITR"))
        }

        //company
        binding.cvCompany.setOnClickListener {
            addFragment(requireActivity(),
                Services2(), bundleOf("type" to "9","title" to "Company"))
        }

        //accounting
        binding.cvAccounting.setOnClickListener {
            addFragment(requireActivity(),
                Services2(), bundleOf("type" to "10","title" to "Accounting & Balance Sheet"))
        }

        //dsc
        binding.cvDsc.setOnClickListener {
            addFragment(requireActivity(),
                Services2(), bundleOf("type" to "11","title" to "DSC"))
        }
        //trademark
        binding.cvTrademark.setOnClickListener {
            addFragment(requireActivity(),
                Services2(), bundleOf("type" to "12","title" to "Trademark"))
        }
        //GST Returns
        binding.cvGstReturn.setOnClickListener {
            addFragment(requireActivity(),
                Services2(), bundleOf("type" to "13","title" to "GST Returns"))
        }

        //compliance's
        binding.cvCompliances.setOnClickListener {
            addFragment(requireActivity(),
                Services2(), bundleOf("type" to "16","title" to "Compliances"))
        }
        binding.other.setOnClickListener {
            addFragment(requireActivity(), Services3())
        }

        binding.setFinancialServices.setOnClickListener {
            addFragment(requireActivity(), Services4())
        }

        binding.setMicroServices.setOnClickListener {
            addFragment(requireActivity(), Services5())
        }
    }


}


class ServicesModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!


    fun getUser() {
        user = sharedPreference.getUser()!!
    }
}



