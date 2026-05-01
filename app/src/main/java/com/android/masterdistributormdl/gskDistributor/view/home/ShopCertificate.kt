package com.android.masterdistributormdl.gskDistributor.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ShopCertificateBinding
import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.JsonObj
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR1
import com.android.masterdistributormdl.gskDistributor.utils.setOnBackResult
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.MainActivity


class ShopCertificate : Fragment() {
    private lateinit var binding: ShopCertificateBinding

    lateinit var model: AddShopModel
    lateinit var shopid: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.shop_certificate, container, false)
        model = ViewModelProvider(this).get(AddShopModel::class.java)
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
            (activity as MainActivity).setHeader("", STATUS_COLOR1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }
        model.isDialogVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireActivity(), it)
        }
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
        binding.save.setOnClickListener {
            downloadShopCerticate()
        }
        setData()
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isEnabled) {
                isEnabled = false
                setOnBackResult(requireActivity(), "ShopCertificate")
            }
        }
    }

    fun setData() {
        val data = requireArguments().getString("data")!!
        val json = JsonObj(data)
        binding.name.setText(json.getString("contact_name"))
        binding.mobile.setText(json.getString("contact_no"))
        binding.shop.setText(json.getString("shop"))
        binding.gstNo.setText(json.getString("gstin"))
        binding.pincode.setText(json.getString("pincode"))
        binding.address.setText(json.getString("address"))
        binding.city.setText(json.getString("city"))
        binding.district.setText(json.getString("district"))
        binding.state.setText(json.getString("state"))
        binding.landmark.setText(json.getString("landmark"))
        shopid = json.getString("shopid")
    }

    private fun downloadShopCerticate() {
        model.shopCerticate(shopid) { url ->
            if (url.isNullOrEmpty()) {
                showToastShort("certificate not created")
            } else {
                val downloader = DownloadFIle()
                downloader.download(requireContext(), url)
            }
        }
    }


}






