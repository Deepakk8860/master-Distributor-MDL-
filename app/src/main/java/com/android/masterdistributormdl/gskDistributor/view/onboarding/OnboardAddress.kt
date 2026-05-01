package com.android.masterdistributormdl.gskDistributor.view.onboarding

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity.RESULT_OK
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.OnboardAddressBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.android.masterdistributormdl.gskDistributor.model.User

import com.gsk.distributor.model.StateItem
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.replaceFragment
import com.android.masterdistributormdl.gskDistributor.utils.setClearError
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.home.Home


class OnboardAddress : Fragment() {
    private lateinit var binding: OnboardAddressBinding
    lateinit var model: OnboardModel
    val states = ArrayList<StateItem>()
    var addType = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.onboard_address, container, false)
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
        setClearError(binding.homeAddLay, binding.etHomeAdd)
        setClearError(binding.homePinLay, binding.etHomePin)
        setClearError(binding.homeStateLay, binding.etHomeState)
        setClearError(binding.homeCityLay, binding.etHomeCity)
        setClearError(binding.busiAddLay, binding.etBusiAdd)
        setClearError(binding.busiPincodeLay, binding.etBusiPincode)
        setClearError(binding.busiStateLay, binding.etBusiState)
        setClearError(binding.busiCityLay, binding.etBusiCity)
        binding.etHomePin.addTextChangedListener {
            if (binding.etHomePin.length() == 6) {
                getPinLocation(binding.etHomePin, binding.etHomeState)
            }
        }
        binding.etBusiPincode.addTextChangedListener {
            if (binding.etBusiPincode.length() == 6) {
                getPinLocation(binding.etBusiPincode, binding.etBusiState)
            }
        }
        binding.etHomeState.setOnClickListener {
            droupDialog(binding.etHomeState, states as ArrayList<Any>)
        }
        binding.etBusiState.setOnClickListener {
            droupDialog(binding.etBusiState, states as ArrayList<Any>)
        }
        binding.back.setOnClickListener {
            replaceFragment(requireActivity(), OnboardBasic())
        }
        binding.next.setOnClickListener { check() }
        (requireActivity() as OnboardActivity).keyboard(binding.mainLayout, binding.bottomLay)
        binding.addressRG.setOnCheckedChangeListener { radioGroup, i ->
            if (binding.homeRB.isChecked) {
                addType = "Home Address"
                binding.homeRB.buttonTintList = (ColorStateList.valueOf(Color.parseColor("#F86202")))
                binding.businessRB.buttonTintList = ColorStateList.valueOf(Color.parseColor("#1D3667"))
            } else {
                addType = "Office Address"
                binding.homeRB.buttonTintList = ColorStateList.valueOf(Color.parseColor("#1D3667"))
                binding.businessRB.buttonTintList = ColorStateList.valueOf(Color.parseColor("#F86202"))
            }
        }
        binding.businessRB.isChecked = true
        setData()
        binding.sameBusiClick.setOnClickListener {
            if (binding.busiLay.visibility == View.GONE) {
                binding.busiLay.visibility = View.VISIBLE
                binding.sameBusiCheck.setBackgroundResource(R.drawable.un_checked_add)
                binding.etBusiAdd.setText("")
                binding.etBusiPincode.setText("")
                binding.etBusiState.setText("")
                binding.etBusiCity.setText("")
            } else {
                sameAsHome()
            }
        }
        statewisecode()
    }

    private fun statewisecode() {
        model.statewisecode {
            states.clear()
            if (it.status == 0) {
                states.addAll(it.state)
            } else {
                AlertError.show(requireContext(), it.message) {
                    statewisecode()
                }
            }
        }
    }

    fun setData() {
        val user = model.getUser()!!
        binding.etHomeAdd.setText(user.home_address)
        binding.etHomePin.setText(user.home_pincode)
        binding.etHomeState.setText(user.home_state)
        binding.etHomeCity.setText(user.home_city)
        binding.etBusiAdd.setText(user.ofc_address)
        binding.etBusiPincode.setText(user.ofc_pincode)
        binding.etBusiState.setText(user.ofc_state)
        binding.etBusiCity.setText(user.ofc_city)
        if (user.sameaddress) {
            binding.busiLay.visibility = View.GONE
            binding.sameBusiCheck.setBackgroundResource(R.drawable.checked_add)
        }

        (requireActivity() as OnboardActivity).setNavigation(user.kyc_status!!, 2,user.video)
    }

    fun sameAsHome() {

        if (binding.etHomeAdd.text.toString().isEmpty()) {
            binding.homeAddLay.requestFocus()
            binding.homeAddLay.error = " "
        } else if (binding.etHomePin.length() != 6) {
            binding.homePinLay.requestFocus()
            binding.homePinLay.error = " "
        } else if (binding.etHomeState.text.toString().isEmpty()) {
            binding.homeStateLay.requestFocus()
            binding.homeStateLay.error = " "
        } else if (binding.etHomeCity.text.toString().isEmpty()) {
            binding.homeCityLay.requestFocus()
            binding.homeCityLay.error = " "
        } else {
            binding.busiLay.visibility = View.GONE
            binding.sameBusiCheck.setBackgroundResource(R.drawable.checked_add)
        }

    }

    fun check() {
        if (binding.etHomeAdd.text.toString().isEmpty()) {
            binding.homeAddLay.requestFocus()
            binding.homeAddLay.error = " "
        } else if (binding.etHomePin.length() != 6) {
            binding.homePinLay.requestFocus()
            binding.homePinLay.error = " "
        } else if (binding.etHomeState.text.toString().isEmpty()) {
            binding.homeStateLay.requestFocus()
            binding.homeStateLay.error = " "
        } else if (binding.etHomeCity.text.toString().isEmpty()) {
            binding.homeCityLay.requestFocus()
            binding.homeCityLay.error = " "
        } else if (binding.busiLay.visibility == View.VISIBLE) {
            if (binding.etBusiAdd.text.toString().isEmpty()) {
                binding.busiAddLay.requestFocus()
                binding.busiAddLay.error = " "
            } else if (binding.etBusiPincode.text.toString().isEmpty()) {
                binding.busiPincodeLay.requestFocus()
                binding.busiPincodeLay.error = " "
            } else if (binding.etBusiState.text.toString().isEmpty()) {
                binding.busiStateLay.requestFocus()
                binding.busiStateLay.error = " "
            } else if (binding.etBusiCity.text.toString().isEmpty()) {
                binding.busiCityLay.requestFocus()
                binding.busiCityLay.error = " "
            } else {
                update_address()
            }
        } else {
            update_address()
        }
    }

    fun update_address() {
        val param = JsonObject()
        param.addProperty("address", binding.etHomeAdd.text.toString())
        param.addProperty("pincode", binding.etHomePin.text.toString())
        param.addProperty("state", binding.etHomeState.text.toString())
        param.addProperty("city", binding.etHomeCity.text.toString())
        if (binding.busiLay.visibility == View.VISIBLE) {
            param.addProperty("sameaddress", 1)
            param.addProperty("ofc_address", binding.etBusiAdd.text.toString())
            param.addProperty("ofc_pincode", binding.etBusiPincode.text.toString())
            param.addProperty("ofc_state", binding.etBusiState.text.toString())
            param.addProperty("ofc_city", binding.etBusiCity.text.toString())
        } else {
            param.addProperty("sameaddress", 0)
            param.addProperty("ofc_address", binding.etHomeAdd.text.toString())
            param.addProperty("ofc_pincode", binding.etHomePin.text.toString())
            param.addProperty("ofc_state", binding.etHomeState.text.toString())
            param.addProperty("ofc_city",binding.etHomeCity.text.toString())
        }
        param.addProperty("licence_address", addType)

        model.update_address(param) {
            if (it.status == 0) {
                if (requireActivity() is OnboardActivity) {
                    (requireActivity() as OnboardActivity).getUserProfile()
                }
//                getUserProfile()
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }

    }

    fun getUserProfile() {
        model.getUserProfile {
            if (it.status == 0) {
                model.save(it.data)
//                replaceFragment(requireActivity(), OnboardPan())
//                replaceFragment(requireActivity(), VideoKyc())
                replaceFragment(requireActivity(),
                    com.android.masterdistributormdl.gskDistributor.view.home.Home()
                )//                replaceFragment(requireActivity(), Agreement(), bundleOf("from" to "onboard"))
            }
            else if (it.status==100){
                //handle maintenance
            }
            else {
                AlertError.show(requireActivity(), it.message) {}
            }
        }
    }



    private fun getPinLocation(editText: EditText, state: EditText) {
        model.getPinLocation(editText.text.toString()) {
            if (it.get("status").asInt == 0) {
                val data = it.get("data").asJsonObject
                state.setText(data.get("statename").asString)
            } else {
                editText.setText("")
                model.errorMessage.value = it.get("message").asString
            }
        }
    }

    private fun droupDialog(editText: EditText, array: ArrayList<Any>) {
        if (array.size == 0) {
            showToastShort("Please wait ...")
            return
        }
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.droup_alert)
        val sheet = dialog.findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet)
        sheet?.setBackgroundColor(Color.TRANSPARENT)
        dialog.show()
        val close = dialog.findViewById<ImageView>(R.id.close)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = HomeAdapter(model, 19)
        adapter.updateAdapter(array)
        recyclerView?.adapter = adapter
        adapter.setOnclickListener {
            it as StateItem
            dialog.dismiss()
            editText.setText(it.gst_state)
        }

        close?.setOnClickListener {
            dialog.dismiss()
        }


    }

}





