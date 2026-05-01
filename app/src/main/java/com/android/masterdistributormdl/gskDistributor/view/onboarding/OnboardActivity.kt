package com.android.masterdistributormdl.gskDistributor.view.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.OnboardActivityBinding
import com.android.masterdistributormdl.gskDistributor.model.KycStatus
import com.android.masterdistributormdl.gskDistributor.model.User
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR1
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_WHITE
import com.android.masterdistributormdl.gskDistributor.utils.Utils
import com.android.masterdistributormdl.gskDistributor.utils.getDialog
import com.android.masterdistributormdl.gskDistributor.utils.replaceFragment
import com.android.masterdistributormdl.gskDistributor.utils.setFontFamily
import com.android.masterdistributormdl.gskDistributor.utils.setTextColor2
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.gsk.distributor.model.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class OnboardActivity : FragmentActivity() {
    lateinit var model: OnboardModel
    private lateinit var binding: OnboardActivityBinding
//    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this)[OnboardModel::class.java]
        binding = OnboardActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
//        firebaseAnalytics = Firebase.analytics
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        model.retrofitError.observe(this) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else {
                InternetError.show(this)
            }
        }
        model.errorMessage.observe(this) {
            AlertError.show(this, it) {}
        }
        model.isLoaderVisible.observe(this) {
            Loading.showHide(this, it)
        }
        binding.mainContent.removeAllViews()
        getUserProfile()
    }

    fun getUserProfile() {
        model.getUserProfile {
            if (it.status == 0) {
                model.save(it.data)
                setFragment(it.data)
            }
            else if (it.status==100){
                //handle maintenance
            }
            else {
                AlertError.show(this, it.message) {}
            }
        }
    }

    private fun setFragment(user: User) {
//        else if (!it.pan_status) {
//            replaceFragment(this, OnboardPan())
//        }
//        else if (!it.aadhaar_status) {
//            replaceFragment(this, OnboardAadhar())
//        }

//        else if (!user.video) {
//            replaceFragment(this, VideoKyc())
//        }

        val it = user.kyc_status!!
        if (!it.basic_status) {
            replaceFragment(this, OnboardBasic())
        } else if (!it.address_status) {
            replaceFragment(this, OnboardAddress())
        }
        else {
            setOnboardingResult()
        }

    }

    private fun setOnboardingResult() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
        finish()
    }

    fun setNavigation(it: KycStatus, int: Int, video:Boolean) {
        binding.radio1.setImageResource(R.drawable.stage_radio2)
        binding.line1.setImageResource(R.drawable.stage_line2)
        binding.basicDetails.setTextColor2("#1D3667")
        binding.videoKyc.setTextColor2("#1D3667")
        binding.homeBusiAdd.setTextColor2("#8A8A8A")
        binding.panCardDetails.setTextColor2("#8A8A8A")
        binding.aadharCardDetails.setTextColor2("#8A8A8A")
        binding.basicDetails.setFontFamily(R.font.medium)
        binding.homeBusiAdd.setFontFamily(R.font.medium)
        binding.panCardDetails.setFontFamily(R.font.medium)
        binding.aadharCardDetails.setFontFamily(R.font.medium)
        binding.videoKyc.setFontFamily(R.font.medium)


        if (it.basic_status) {
            binding.radio2.setImageResource(R.drawable.stage_radio2)
            binding.line2.setImageResource(R.drawable.stage_line2)
            binding.homeBusiAdd.setTextColor2("#1D3667")
        }
        if (it.address_status) {
            binding.radio3.setImageResource(R.drawable.stage_radio2)
            binding.line3.setImageResource(R.drawable.stage_line2)
            binding.panCardDetails.setTextColor2("#1D3667")
        }
        if (it.pan_status) {
            binding.radio4.setImageResource(R.drawable.stage_radio2)
            binding.line5.setImageResource(R.drawable.stage_line2)
            binding.aadharCardDetails.setTextColor2("#1D3667")
        }
        if (video){
            binding.radio5.setImageResource(R.drawable.stage_radio2)
            binding.videoKyc.setTextColor2("#1D3667")
        }
        if (int == 1) {
            binding.basicDetails.setFontFamily(R.font.bold)
        } else if (int == 2) {
            binding.homeBusiAdd.setFontFamily(R.font.bold)
        } else if (int == 3) {
            binding.panCardDetails.setFontFamily(R.font.bold)
        } else if (int == 4) {
            binding.aadharCardDetails.setFontFamily(R.font.bold)
        }
        else if (int == 5) {
            binding.videoKyc.setFontFamily(R.font.bold)
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    private fun getLastLocation() {
        if (isLocationEnabled()) {
            getPermission()
        } else {
            val dialog = getDialog(this, R.layout.alert_button_dialog)
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

    fun keyboard(mainLayout: View, actionView:View) {
        mainLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rec = Rect()
            mainLayout.getWindowVisibleDisplayFrame(rec)
            val screenHeight = mainLayout.rootView.height
            val keypadHeight = screenHeight - rec.bottom
            if (keypadHeight > screenHeight * 0.15) {
                actionView.visibility = View.GONE
            } else {
                actionView.visibility = View.VISIBLE
            }
        }
    }


    private fun getPermission() {
        val dexter = Dexter.withContext(this).withPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                report.let {
                    if (report.areAllPermissionsGranted()) {
                        setLocation()
                    } else {
                        Utils.openAppSetting(
                            this@OnboardActivity, "Please allowed location permission"
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
        fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            val location = task.result
            if (location == null) {
                val locationRequest = LocationRequest.create().apply {
                    interval = 100
                    fastestInterval = 0
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                    maxWaitTime = 100
                    numUpdates = 1
                }
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onResume() {
        super.onResume()
//        getLastLocation()
    }


    fun setHeader(type: String, color: String = STATUS_COLOR1) {
        statusBarColor(color)
        if (type == "Onboard") {
            binding.onbordStageLay.visibility = View.VISIBLE
        } else {
            binding.onbordStageLay.visibility = View.GONE
        }
    }


    private fun statusBarColor(color: String) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor(color)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            color != STATUS_COLOR2
        window.navigationBarColor = Color.parseColor(STATUS_WHITE)
    }


}






