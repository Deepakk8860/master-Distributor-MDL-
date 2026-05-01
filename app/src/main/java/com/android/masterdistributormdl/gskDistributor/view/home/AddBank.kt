package com.android.masterdistributormdl.gskDistributor.view.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.AddBankBinding

import com.google.gson.JsonObject

import com.android.masterdistributormdl.gskDistributor.model.BankData
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.SuccessAlert
import com.android.masterdistributormdl.gskDistributor.utils.Utils
import com.android.masterdistributormdl.gskDistributor.utils.checkIfsc
import com.android.masterdistributormdl.gskDistributor.utils.checkUpiId
import com.android.masterdistributormdl.gskDistributor.utils.getDialog
import com.android.masterdistributormdl.gskDistributor.utils.getHtmlSpanned
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.setClearError
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class AddBank : Fragment() {
    lateinit var model: KycModel
    private lateinit var binding: AddBankBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.add_bank, container, false)
        model = ViewModelProvider(this)[KycModel::class.java]
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

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
        binding.save.setOnClickListener { check() }
        binding.back.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }



        binding.bankTab.setOnClickListener {
            selectedTabView = binding.bankTab
            setTabView()
        }
        binding.upiTab.setOnClickListener {
            selectedTabView = binding.upiTab
            setTabView()
        }
        selectedTabView = binding.bankTab
        setTabView()

        setClearError(binding.upiNameLay, binding.upiName)
        setClearError(binding.upiIdLay, binding.upiId)
        setClearError(binding.bankNameLay, binding.bankName)
        setClearError(binding.accountNo1Lay, binding.accountNo1)
        setClearError(binding.accountNo2Lay, binding.accountNo2)
        setClearError(binding.ifscCodeLay, binding.ifscCode)
        setClearError(binding.accountHolderLay, binding.accountHolder)
        getLastLocation()
        getTextWatcher()
    }

    private fun getTextWatcher() {
        binding.ifscCode.addTextChangedListener {
            if (checkIfsc(binding.ifscCode.text.toString())){
                getIfscData()
            }
        }
    }

    private fun getLastLocation() {
        if (isLocationEnabled()) {
            getPermission()
        } else {
            val dialog = getDialog(requireContext(), R.layout.alert_button_dialog)
            dialog.setCancelable(false)
            val msg = dialog.findViewById<TextView>(R.id.msg)
            val ok = dialog.findViewById<Button>(R.id.ok)
            ok.text = "Go to Settings"
            msg.text = "To continue, turn on device location."
            ok.setOnClickListener {
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
            dialog.show()

        }

    }

    private fun getPermission() {
        val dexter = Dexter.withContext(requireContext()).withPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                report.let {
                    if (report.areAllPermissionsGranted()) {
                        setLocation()
                    } else {
                        Utils.openAppSetting(
                            requireContext(), "Please allowed location permission"
                        )
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: List<PermissionRequest?>?, token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }
        }).withErrorListener {
            showToastShort(it.name)
        }
        dexter.check()
    }

    @SuppressLint("MissingPermission")
    fun setLocation() {
        fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
            val location = task.result
            if (location == null) {
                val locationRequest = LocationRequest.create().apply {
                    interval = 100
                    fastestInterval = 0
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                    maxWaitTime = 100
                    numUpdates = 1
                }
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                fusedLocationClient.requestLocationUpdates(
                    locationRequest, object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            val location = locationResult.lastLocation
                            if (location != null) {
                                model.setLatLong(location.latitude, location.longitude)
                            }

                        }
                    }, Looper.myLooper()
                )
            } else {
                model.setLatLong(location.latitude, location.longitude)

            }
        }

    }


    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private lateinit var selectedTabView: TextView
    private fun setTabView() {
        binding.bankTab.setBackgroundResource(R.color.trans)
        binding.upiTab.setBackgroundResource(R.color.trans)
        selectedTabView.setBackgroundResource(R.drawable.tab_selector)
        binding.bankTab.setTextColor(Color.parseColor("#801D3667"))
        binding.upiTab.setTextColor(Color.parseColor("#801D3667"))
        selectedTabView.setTextColor(Color.parseColor("#1D3667"))
        if (selectedTabView == binding.bankTab) {
            binding.bankLay.visibility = View.VISIBLE
            binding.upiLay.visibility = View.GONE
            binding.tabName.text = "Bank Details"
        } else {
            binding.bankLay.visibility = View.GONE
            binding.upiLay.visibility = View.VISIBLE
            binding.tabName.text = "Beneficiary UPI ID Details"
        }
    }


    fun check() {
        if (selectedTabView == binding.upiTab) {
              if (binding.upiName.text.toString().isEmpty()) {
                binding.upiNameLay.requestFocus()
                binding.upiNameLay.error = " "
            } else if (binding.upiId.text.toString().isEmpty()) {
                  binding.upiIdLay.requestFocus()
                  binding.upiIdLay.error = " "
            } else if (!checkUpiId(binding.upiId.text.toString())) {
                  binding.upiIdLay.requestFocus()
                  binding.upiIdLay.error = " "
                showToastShort("Please enter valid UPI ID")
            } else {
                val param = JsonObject()
                param.addProperty("account_name", binding.upiName.text.toString())
                param.addProperty("account_number", binding.upiId.text.toString())
                param.addProperty("type", "UPI")
                alert(param)
            }
        } else {
             if (binding.ifscCode.text.toString().isEmpty()) {
                binding.ifscCodeLay.requestFocus()
                binding.ifscCodeLay.error = " "
            } else if (!checkIfsc(binding.ifscCode.text.toString())) {
                binding.ifscCodeLay.requestFocus()
                binding.ifscCodeLay.error = " "
            }
           else if (binding.bankName.text.toString().isEmpty()) {
                binding.bankNameLay.requestFocus()
                binding.bankNameLay.error = " "
            } else if (binding.accountNo1.text.toString().isEmpty()) {
                binding.accountNo1Lay.requestFocus()
                binding.accountNo1Lay.error = " "
            } else if (binding.accountNo2.text.toString().isEmpty()) {
                binding.accountNo2Lay.requestFocus()
                binding.accountNo2Lay.error = " "
            } else if (binding.accountNo2.text.toString() != binding.accountNo1.text.toString()) {
                showToastShort("Both account numbers are not the same")
            }  else if (binding.accountHolder.text.toString().isEmpty()) {
                binding.accountHolderLay.requestFocus()
                binding.accountHolderLay.error = " "
            } else {
                val param = JsonObject()
                param.addProperty("account_name", binding.accountHolder.text.toString())
                param.addProperty("account_number", binding.accountNo1.text.toString())
                param.addProperty("ifsc_code", binding.ifscCode.text.toString())
                param.addProperty("bank_name", binding.bankName.text.toString())
                param.addProperty("type", "ACC")
                alert(param)
            }
        }


    }

    private fun alert(param: JsonObject) {
        val dialog = getDialog(requireContext(), R.layout.bank_alert)
        val msg = dialog.findViewById<TextView>(R.id.msg)
        val no = dialog.findViewById<TextView>(R.id.no)
        val yes = dialog.findViewById<TextView>(R.id.yes)
        val number = param.get("account_number").asString
        val message =
            "Your Commission will be sent to <b>$number</b>. Note that this setting can not be changed once saved."
        msg.text = getHtmlSpanned(message)
        no.setOnClickListener {
            dialog.dismiss()

        }
        yes.setOnClickListener {
            dialog.dismiss()
            addBankDetails(param)
        }
        dialog.show()
    }

    private fun getIfscData() {
        val param=JsonObject()
        param.addProperty("ifsc_code",binding.ifscCode.text.toString())
        model.getIfscData(param) {
            if (it.status == 0) {
                setBank(it.bank_data)
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }
    }

    private fun setBank(it: BankData) {
        binding.bankName.setText(it.bankName)
    }

    private fun addBankDetails(param: JsonObject) {
        model.addBankDetails(param) {
            if (it.status == 0) {
                SuccessAlert.show(requireActivity(), it.message) {
                    requireActivity().onBackPressed()
                }
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }
    }
}





