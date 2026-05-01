package com.android.masterdistributormdl.gskDistributor.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.masterdistributormdl.BuildConfig
import com.android.masterdistributormdl.databinding.LayoutUnblockBinding
import com.android.masterdistributormdl.databinding.MainActivityBinding
import com.android.masterdistributormdl.databinding.MainActivityDistBinding


import com.google.android.gms.location.*
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.android.masterdistributormdl.gskDistributor.model.PushNoti
import com.android.masterdistributormdl.gskDistributor.model.User
import com.gsk.distributor.model.*
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.APP_TYPE
import com.android.masterdistributormdl.gskDistributor.utils.Alert
import com.android.masterdistributormdl.gskDistributor.utils.BASE_URL
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.NOTIFICATIONS_RECEIVER
import com.android.masterdistributormdl.gskDistributor.utils.SHARE_URL
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR1
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_WHITE
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.addFragmentFade
import com.android.masterdistributormdl.gskDistributor.utils.device_token
import com.android.masterdistributormdl.gskDistributor.utils.getAppFragmentManager
import com.android.masterdistributormdl.gskDistributor.utils.getDialog
import com.android.masterdistributormdl.gskDistributor.utils.getStringAssets
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.imageToBase64
import com.android.masterdistributormdl.gskDistributor.utils.is_login
import com.android.masterdistributormdl.gskDistributor.utils.is_sales
import com.android.masterdistributormdl.gskDistributor.utils.latitude
import com.android.masterdistributormdl.gskDistributor.utils.loadImage
import com.android.masterdistributormdl.gskDistributor.utils.longitude
import com.android.masterdistributormdl.gskDistributor.utils.m_pin
import com.android.masterdistributormdl.gskDistributor.utils.replaceFragment
import com.android.masterdistributormdl.gskDistributor.utils.setEditText
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.stringToBase64
import com.android.masterdistributormdl.gskDistributor.utils.user_data
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.home.AddSales
import com.android.masterdistributormdl.gskDistributor.view.home.BannerImage
import com.android.masterdistributormdl.gskDistributor.view.home.BusinessProposalPdf
import com.android.masterdistributormdl.gskDistributor.view.home.Home
import com.android.masterdistributormdl.gskDistributor.view.home.KycUpdate
import com.android.masterdistributormdl.gskDistributor.view.home.NotificationSettings
import com.android.masterdistributormdl.gskDistributor.view.home.Offer
import com.android.masterdistributormdl.gskDistributor.view.home.ProfileInfo
import com.android.masterdistributormdl.gskDistributor.view.home.Report
import com.android.masterdistributormdl.gskDistributor.view.home.ReportDownloads
import com.android.masterdistributormdl.gskDistributor.view.home.ReportEarning
import com.android.masterdistributormdl.gskDistributor.view.home.ReportOrder
import com.android.masterdistributormdl.gskDistributor.view.home.Services
import com.android.masterdistributormdl.gskDistributor.view.home.Shop
import com.android.masterdistributormdl.gskDistributor.view.home.Support
import com.android.masterdistributormdl.gskDistributor.view.home.TicketInfo
import com.android.masterdistributormdl.gskDistributor.view.home.TrainingVideoList
import com.android.masterdistributormdl.gskDistributor.view.home.ViewCertificate
import com.android.masterdistributormdl.gskDistributor.view.home.ViewProfilePhoto
import com.android.masterdistributormdl.gskDistributor.view.home.VisitingCard
import com.android.masterdistributormdl.gskDistributor.view.onboarding.Agreement
import com.android.masterdistributormdl.gskDistributor.view.onboarding.OnboardActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.cropper.CropImageContract
import com.android.masterdistributormdl.cropper.CropOptions
import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.android.masterdistributormdl.gskDistributor.utils.Utils
import com.android.masterdistributormdl.gskDistributor.utils.intentPass
import com.android.masterdistributormdl.gskDistributor.view.home.ConsultantDetails
import com.android.masterdistributormdl.gskDistributor.view.home.GskDistributor
import com.android.masterdistributormdl.gskDistributor.view.home.TrainingStatus
import com.android.masterdistributormdl.login.Login
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.main.SplashActivity
import com.android.masterdistributormdl.utils.MyApplication.Companion.logoutAlreadyHandled
import com.android.masterdistributormdl.utils.SharedPreference
import com.android.masterdistributormdl.utils.showToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.pushwoosh.Pushwoosh
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

const val FCM_SENDER_ID = "385487100197"
const val PUSH_APP_ID = "08CFE-96271"

class MainActivity : FragmentActivity() {
    private lateinit var binding: MainActivityDistBinding
    lateinit var model: MainModel
    private var dialogRes: ApiResponse1? = null
    private var isTabPage = true
    private lateinit var logoutReceiver: LogoutReceiver
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 123
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var authtReceiver: AuthReceiver
    //    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val pushwoosh = Pushwoosh.getInstance()
    private var profilePhoto=""


    private fun setPushWoosh() {
        pushwoosh.senderId = FCM_SENDER_ID
        pushwoosh.appId = PUSH_APP_ID
        pushwoosh.registerForPushNotifications()
    }

    fun unregisterPushNotifications() {
        pushwoosh.unregisterForPushNotifications()
    }


    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this)[MainModel::class.java]
        binding = MainActivityDistBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setPushWoosh()

        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
//        firebaseAnalytics = Firebase.analytics
        // Load an Ad Request (Example)
        // Simulate sending a conversion event
//        trackAdConversion("8736282928", "TestDistributor")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        // Initialize Google Sign-In Client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        //getBlockStatus()
//        binding.bottomView.visibility = View.GONE
        model.bottomAdapter.setOnclickListener {
            it as BottomMenu
            if (it.id == 1) {
                replaceFragment(this, Home())
            } else if (it.id == 2) {
                val bundle = bundleOf("type" to "report", "title" to "Distributor Report")
                addFragment(this, GskDistributor(), bundle)
            } else if (it.id == 3) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
            } else if (it.id == 4) {
                replaceFragment(this, Services())
            } else if (it.id == 5) {
                replaceFragment(this, Report())
            }
        }
        //clearFragments()
        setFragment()

        binding.menu.close.setOnClickListener { binding.drawerLayout.closeDrawers() }
        binding.menu.logout.setOnClickListener { openByDrawer(it) }
        binding.menu.privacyPolicy.setOnClickListener { openByDrawer(it) }
        binding.menu.trainingStatus.setOnClickListener { openByDrawer(it) }
        binding.menu.termsCondition.setOnClickListener { openByDrawer(it) }
        binding.menu.referList.setOnClickListener { openByDrawer(it) }
        binding.menu.support.setOnClickListener { openByDrawer(it) }
        binding.menu.unblockCrm.setOnClickListener { openByDrawer(it) }
        binding.menu.refundPolicy.setOnClickListener { openByDrawer(it) }
        binding.menu.myProfile.setOnClickListener { openByDrawer(it) }
        binding.menu.promote.setOnClickListener { openByDrawer(it) }
        binding.menu.referFriend.setOnClickListener { openByDrawer(it) }
        binding.menu.notificationSetting.setOnClickListener { openByDrawer(it) }
        binding.menu.llVisitingCard.setOnClickListener { openByDrawer(it) }
        binding.menu.share.setOnClickListener { openByDrawer(it) }
        binding.menu.shareProposal.setOnClickListener { openByDrawer(it) }
        binding.menu.promoteList.setOnClickListener { openByDrawer(it) }
        binding.menu.llTrainings.setOnClickListener { openByDrawer(it) }
        binding.menu.userImage.setOnClickListener { openByDrawer(it) }
        //hide when app type master franchise other wise show(mf01) refer and share
//        binding.menu.referFriend.visibility = View.VISIBLE
        binding.menu.share.visibility = View.GONE
//        binding.menu.share.visibility = View.VISIBLE
        binding.menu.offers.setOnClickListener { openByDrawer(it) }
        binding.menu.agreement.setOnClickListener { openByDrawer(it) }
        binding.menu.addSalesAgent.setOnClickListener { openByDrawer(it) }
        binding.menu.viewCertificate.setOnClickListener { openByDrawer(it) }
        // addSalesAgent.visibility = View.GONE
        binding.drawerLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rec = Rect()
            binding.drawerLayout.getWindowVisibleDisplayFrame(rec)
            val screenHeight = binding.drawerLayout.rootView.height
            val keypadHeight = screenHeight - rec.bottom
            if (isTabPage) {
                if (keypadHeight > screenHeight * 0.15) {
                    binding.bottomView.visibility = View.GONE
                } else {
                    binding.bottomView.visibility = View.VISIBLE
                }
            }
        }
        model.isLoaderVisible.observe(this) {
            Loading.showHide(this, it)
        }
//        binding.menu.userImage.setOnClickListener {
//            binding.drawerLayout.closeDrawers()
//            onBackResultProfile("viewProfile")
//            addFragment(this,ViewProfilePhoto(), bundleOf("profilePhoto" to profilePhoto))
////            startCrop()
//        }


    }

    private fun trackAdConversion(conversionId: String, conversionLabel: String) {
        val bundle = Bundle().apply {
            putString("ad_conversion_id", conversionId)
            putString("ad_conversion_label", conversionLabel)
        }
        Log.d(TAG, "trackAdConversion: ksjsjd")
//        firebaseAnalytics.logEvent("ad_conversion", bundle)
    }

     fun getAppVersion() {
        model.getAppVersion {
            if (it.status == 0) {
                val version = it.app_version
                if (version > BuildConfig.VERSION_CODE) {
                    Alert.updateApp(this) {
                        val url = it.app_link
                        val downloader = DownloadFIle()
                        downloader.download(this, url)
                    }
                }
            }

        }

    }



    private fun getFirebaseToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result
                saveFirebaseToken(token)
            }else{
                Log.d("deepakFirebaseToken","fail")
            }
        }
    }

    private fun saveFirebaseToken(token: String?) {
        val param=JsonObject()
        param.addProperty("token",token)
        model.saveFirebaseToken(param){
            if (it.status==0){
                Log.d(TAG, "saveFirebaseToken: saved successfully")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterLogoutReceiver()
        unRegisterAuthReceiver()
    }

    private fun openByDrawer(view: View) {
        binding.drawerLayout.closeDrawers()
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(300))
            withContext(Dispatchers.Main) {
                openByDrawer2(view)
            }
        }
    }

    fun setAddSale() {
        if (is_sales) {
            binding.menu.addSalesAgent.visibility = View.VISIBLE
        } else {
            binding.menu.addSalesAgent.visibility = View.GONE
        }
    }

    fun checkOnboarding() {
        val user = model.getUser()!!
//                || !user.video
//                || !key.pan_status || !key.aadhaar_status
        val key = user.kyc_status
        if (key != null) {
            if (!key.basic_status || !key.address_status || !key.agreement_status) {
                val intent = Intent(this, OnboardActivity::class.java)
                onboardLauncher.launch(intent)
            } else {
//                val intent = Intent(this, OnboardActivity::class.java)
//                onboardLauncher.launch(intent)
                replaceFragment(this, Home())
            }
        } else {
            replaceFragment(this, Home())
        }
    }

    private val onboardLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                replaceFragment(this, Home())
            } else {
                finish()
            }
        }

    private fun setHome() {
        Log.d(TAG, "handleMenuVisibility:Home ")
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(200))
            withContext(Dispatchers.Main) {
                replaceFragment(this@MainActivity, Home())
                model.bottomAdapter.selected = 1
                model.bottomAdapter.updateAdapter()
            }
        }
    }

    private fun openByDrawer2(view: View) {
        if (view == binding.menu.privacyPolicy) {
            val end = if (APP_TYPE == "DISTRIBUTOR") {
                "/privacy-policy/distributor"
            } else {
//                "/privacy-policy/mf"
                "/privacy-policy/distributor"
            }
            val bundle = bundleOf("url" to BASE_URL + end,"title" to "Privacy Policy")
            addFragment(this, Content(), bundle)
        } else if (view == binding.menu.termsCondition) {
            val bundle = bundleOf("url" to BASE_URL + "/terms-and-conditions/distributor","title" to "Term and Condition")
            addFragment(this, Content(), bundle)
        } else if (view == binding.menu.refundPolicy) {
            val bundle = bundleOf("url" to BASE_URL + "/refund-policy/distributor","title" to "Refund Policy")
            addFragment(this, Content(), bundle)
        } else if (view == binding.menu.logout) {
            logoutDialog()
        } else if (view == binding.menu.unblockCrm) {
            openDialogForUnblock()
        } else if (view == binding.menu.support) {
            openSupport()
        } else if (view == binding.menu.myProfile) {
            addFragment(this, ProfileInfo())
        } else if (view == binding.menu.viewCertificate) {
            addFragment(this, ViewCertificate())
        } else if (view == binding.menu.shareProposal) {
            addFragment(this, BusinessProposalPdf())
        } else if (view == binding.menu.addSalesAgent) {
            addFragment(this, AddSales())
        } else if (view == binding.menu.promote) {
            addFragment(this, BannerImage())
        } else if (view == binding.menu.llVisitingCard) {
            addFragment(this, VisitingCard())
        } else if (view == binding.menu.llTrainings) {
            addFragment(this, TrainingVideoList())
        } else if (view == binding.menu.agreement) {
            addFragment(this, Agreement(), bundleOf("from" to "menu"))
        } else if (view == binding.menu.share) {
            shareAppLink()
        } else if (view == binding.menu.offers) {
            addFragment(this, Offer())
        }
        else if (view == binding.menu.trainingStatus) {
            addFragment(this, TrainingStatus())
        }
        else if (view == binding.menu.userImage) {
            addFragment(this, ViewProfilePhoto(), bundleOf("profilePhoto" to profilePhoto))
        } else if (view == binding.menu.referFriend) {
//            model.bottomAdapter.selected = 3
//            model.bottomAdapter.updateAdapter()
//            addFragment(this, Refer())
        } else if (view == binding.menu.referList) {
//            addFragment(this, ReferList())
        } else if (view == binding.menu.promoteList) {
//            addFragment(this, PromoteUserList())
        } else if (view == binding.menu.notificationSetting) {
            addFragment(this, NotificationSettings())
        }
    }

    fun openSupport() {
        addFragment(this, Support())
    }

    fun openConsultant() {
        addFragment(this, ConsultantDetails())
    }

    @SuppressLint("RtlHardcoded")
    fun openDrawer() {
        hideSoftKeyBoard(this)
        binding.drawerLayout.openDrawer(Gravity.LEFT)
    }

    private fun openDialogForUnblock() {

        val bindingopen: LayoutUnblockBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this), com.android.masterdistributormdl.R.layout.layout_unblock,
            null,
            false
        )

        val dialog = AlertDialog.Builder(this).apply {
            // Use the inflated layout from binding
            setView(bindingopen.root)
        }.create()
        bindingopen.name.setText(model.getUser()?.fullname)
        bindingopen.mobile.setText(model.getUser()?.mobile)
        bindingopen.email.setText(model.getUser()?.email)
        bindingopen.branchId.setText(dialogRes?.branch_id.toString())
        setEditText(bindingopen.name)
        setEditText(bindingopen.mobile)
        setEditText(bindingopen.email)
        setEditText(bindingopen.branchId)
        bindingopen.close.setOnClickListener {
            dialog.dismiss()
        }

        bindingopen.submit.setOnClickListener {
//            getUnblockStatus {
//                if (it.status == 0) {
//                    dialog.dismiss()
//                    showToastShort(it.message)
////                    getBlockStatus()
//                } else {
//                    dialog.dismiss()
//                    AlertError.show(this, it.message) {}
//                }
//            }
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        checkNotificationPermission()
    }



    //check notification permission
    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // showToastShort("Permission Granted")
            } else {
                // Notification permission denied
            }
        }
    }


    fun getBlockStatus() {
        model.getBlockStatus {
            if (it.status == 0) {
                dialogRes = it
                binding.menu.unblockCrm.visibility = View.VISIBLE
            } else {
                dialogRes = it
                binding.menu.unblockCrm.visibility = View.GONE
            }
        }
    }

    private fun getUnblockStatus(result: (ApiResponse) -> Unit) {
        model.unBloackStatus {
            result.invoke(it)
        }
    }

    fun checkNotifications() {
        val enabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
        if (!enabled) {
            notificationsAlert()
        }
    }

    private fun notificationsAlert() {
        val dialog = getDialog(this, R.layout.noti_alert)

        dialog.setCancelable(false)

        val allow = dialog.findViewById<Button>(R.id.allow)

        val close = dialog.findViewById<ImageView>(R.id.close)

        close.setOnClickListener { dialog.dismiss() }

        allow.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        dialog.show()
    }

    fun setUserData(user: User) {
        model.save(user)
        binding.menu.name.text = user.fullname
        binding.menu.email.text = user.email
        profilePhoto=user.profilephoto
        loadImage(binding.menu.userImage, user.profilephoto, R.drawable.logo_app)
        val userId = user.id
        pushwoosh.setUserId(userId)
    }


    private fun setFragment() {
        val mpin = model.sharedPreference.getString(m_pin)!!
        val isLogin = model.sharedPreference.getBoolean(is_login)

        if (model.getUser() == null) {
            replaceFragment(this, Login())
        }else{
            getFirebaseToken()
            replaceFragment(this,Home())
        }

       /* if (model.getUser() == null) {
//            replaceFragment(this, Login())
        } else if (mpin.length == 4) {
            if (intent.getStringExtra(Pushwoosh.PUSH_RECEIVE_EVENT).isNullOrEmpty()) {
                openLoginPin("start")
            } else {
                checkOnboarding()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(TimeUnit.MILLISECONDS.toMillis(500))
                    withContext(Dispatchers.Main) {
                        openNotification()
                    }
                }
            }
        }*/
    }

    fun handleMenuVisibility() {
        val user = model.getUser()!!
        if (user.is_refer_enable) {
            binding.menu.referFriend.visibility = View.VISIBLE
            binding.menu.referList.visibility = View.VISIBLE
        } else {
            binding.menu.referFriend.visibility = View.GONE
            binding.menu.referList.visibility = View.GONE
        }
    }


    fun getBitmap(): ByteArray {
        val bitmap = Utils.getBitmapFromView(binding.drawerLayout)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        return byteArray

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

    private fun shareAppLink() {
        var str = model.getUser()?.id + "-" + "GSK"
        str = stringToBase64(str)
        val content = SHARE_URL + str
        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "App Link")
        intent.putExtra(Intent.EXTRA_TEXT, content)
        startActivity(Intent.createChooser(intent, "Share Via"))
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
                            this@MainActivity, "Please allowed location permission"
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
        startLogoutReceiver()
        startAuthReceiver()

        startNotificationReceiver()
//        getLastLocation()
    }

    override fun onPause() {
        super.onPause()
        unRegisterNotificationReceiver()
    }

    private lateinit var notificationReceiver: NotificationReceiver

    private fun unRegisterNotificationReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver)
    }

    private fun startNotificationReceiver() {
        notificationReceiver = NotificationReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(NOTIFICATIONS_RECEIVER)
        LocalBroadcastManager.getInstance(this@MainActivity)
            .registerReceiver(notificationReceiver, intentFilter)
    }

    inner class NotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, mIntent: Intent) {
            intent.putExtra(
                Pushwoosh.PUSH_RECEIVE_EVENT,
                mIntent.getStringExtra(Pushwoosh.PUSH_RECEIVE_EVENT)
            )
//            openNotification()

        }
    }


  /*  private fun openLoginPin(type: String) {
        addFragmentFade(this, LoginPin(), bundleOf("type" to type))
        onBackResult(m_pin)
    }*/

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//
//        Log.d(TAG, "Intent: onNewIntent")
//        openNotification()
//    }

    fun openNotification() {
        val pushData = intent?.getStringExtra(Pushwoosh.PUSH_RECEIVE_EVENT)
        if (pushData.isNullOrEmpty()) return
        intent.putExtra(Pushwoosh.PUSH_RECEIVE_EVENT, "")
        val pushNoti: PushNoti = gson.fromJson(pushData, object : TypeToken<PushNoti>() {}.type)
        val eventType = pushNoti.userdata
        val event = eventType?.event
        val id = eventType?.id

        if (id.isNullOrEmpty() || event.isNullOrEmpty()) return

        if (event == "TICKET") {
            val bundle = bundleOf("ticket_id" to id, "data" to null)
            addFragmentFade(this, TicketInfo(), bundle)
        } else if (event == "KYC") {
            addFragmentFade(this, KycUpdate())
        } else if (event == "ORDER") {
            addFragment(this@MainActivity, ReportOrder())
        } else if (event == "REFER") {
//            addFragment(this@MainActivity, ReferList())
        } else if (event == "CUSTOMER") {
            addFragment(this@MainActivity, ReportDownloads(),bundleOf("reference" to "main"))
        } else if (event == "EARN") {
            addFragment(this@MainActivity, ReportEarning())
        }

        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(500))
            withContext(Dispatchers.Main) {
//                openLoginPin("notification")
            }
        }
    }


    private fun onBackResult(result_key: String) {
        getAppFragmentManager(this).setFragmentResultListener(
            result_key, this
        ) { requestKey, bundle ->
            if (requestKey.equals(m_pin)) {
                if (bundle.getInt("result") == 1) {
                    val type = bundle.getString("type")
                    if (type == "start") {
                        checkOnboarding()
                    }
                } else {
                    finish()
                }
            }
        }
    }

    private fun onBackResultProfile(result_key: String) {
        getAppFragmentManager(this).setFragmentResultListener(
            result_key, this
        ) { requestKey, bundle ->

        }
    }


    fun getUser() {
        model.getUser()
    }



    fun setHeader(title: String, color: String = STATUS_COLOR1) {
        binding.bottomView.visibility = View.GONE
        statusBarColor(color)
        isTabPage = false
    }

    fun setHeader2(title: String, color: String = STATUS_COLOR2) {
        binding.bottomView.visibility = View.VISIBLE
        statusBarColor(color)
        isTabPage = true
    }

    private fun statusBarColor(color: String) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor(color)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            color != STATUS_COLOR2
        window.navigationBarColor = Color.parseColor(STATUS_WHITE)
    }

    private fun logoutDialog() {
        val dialog = getDialog(this, R.layout.logout_alert)
        val msg = dialog.findViewById<TextView>(R.id.msg)
        val logout = dialog.findViewById<TextView>(R.id.logout)
        val cancel = dialog.findViewById<TextView>(R.id.cancel)
        msg.text = "Are you sure you want to logout?"
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        logout.setOnClickListener {
            dialog.dismiss()
            pushwoosh.unregisterForPushNotifications()
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    // ✅ Successfully signed out and access revoked
                }
            }
            model.sharedPreference.putBoolean(is_login, false)
            model.sharedPreference.clearSharedPrefernce()
            val intent = Intent(this, com.android.masterdistributormdl.gskDistributor.view.MainActivity::class.java)
            startActivity(intent)
            finish()
//            Loading.show(this@MainActivity)
//            CoroutineScope(Dispatchers.IO).launch {
//                delay(TimeUnit.SECONDS.toMillis(2))
//                withContext(Dispatchers.Main) {
//                    Loading.dismiss()
//                    finish()
//                }
//            }
        }
        dialog.show()

    }

    private fun startAuthReceiver() {
        authtReceiver = AuthReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(AUTH_RECEIVER)
        LocalBroadcastManager.getInstance(this@MainActivity)
            .registerReceiver(authtReceiver, intentFilter)
    }

    private fun unRegisterAuthReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(authtReceiver)
    }

    inner class AuthReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status = intent.getStringExtra("status")
            Log.d(TAG, "onReceive:data $status")
            if (status == "auth") {
                showToast("Your session has expired, please login again!!")
                val intent2 = Intent(context, SplashActivity::class.java)
                intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent2)
                finish()
            } else if (status == "error") {
                val intent2 = Intent(context, ErrorActivityDist::class.java)
                intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent2)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (model.getUser() == null) {
            super.onBackPressed()
            return
        }
        val selected = model.bottomAdapter.selected
        val count = getAppFragmentManager(this).backStackEntryCount
        if (selected == 1 && count == 0) {
            appCloseDialog()
        } else if (selected != 1 && count == 0) {
            setHome()
        } else {
            super.onBackPressed()
        }
    }

    private fun appCloseDialog() {
        val dialog = getDialog(this, R.layout.two_button_alert)
        val msg = dialog.findViewById<TextView>(R.id.msg)
        msg.text = "Are you sure you want to exit?"
        val no = dialog.findViewById<Button>(R.id.no)
        val yes = dialog.findViewById<Button>(R.id.yes)
        no.setOnClickListener {
            dialog.dismiss()
        }
        yes.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }

    private fun startLogoutReceiver() {
        logoutReceiver = LogoutReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(LOGOUT_RECEIVER)
        LocalBroadcastManager.getInstance(this@MainActivity)
            .registerReceiver(logoutReceiver, intentFilter)
    }

    private fun unRegisterLogoutReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(logoutReceiver)
    }

    inner class LogoutReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (model.getUser()!=null){
                if (logoutAlreadyHandled) return
                logoutAlreadyHandled = true
//                clearAppData()
                model.sharedPreference.clearSharedPrefernce()
                showToastShort("Your session has expired, please login again!!")
                val intent2 = Intent(context, SplashActivity::class.java)
                intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent2)
                finish()
            }
        }
    }

    fun clearAppData() {
        model.sharedPreference.clearSharedPrefernce()
        intentPass(this@MainActivity, SplashActivity())
        finish()
    }
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent!!
            val filePath = result.getUriFilePath(this)
            val file = File(filePath)
            val bitmap = BitmapFactory.decodeFile(filePath)
            val base64 = imageToBase64(file)
            file.delete()
            model.uploadprofileimg(base64) {
                binding.menu.userImage.setImageBitmap(bitmap)
            }
        } else {
            val exception = result.error
            // AlertError.show(requireActivity(), exception!!.localizedMessage)
        }
    }

    private fun startCrop() {
        cropImage.launch(CropOptions {
            setAspectRatio(500, 500)
            setActivityTitle("Pick Image")
            setRequestedSize(300, 300)
            setAllowFlipping(true)
            setAllowRotation(true)
            setImageSource(includeGallery = true, includeCamera = true)
        })
    }


   /* fun getAppVersion() {
        model.getAppVersion {
            if (it.status == 0) {
                val version = it.app_version
                if (version > BuildConfig.VERSION_CODE) {
                    Log.d(TAG, "getAppVersion: ${it.app_link}")
                    if (APP_TYPE == "DISTRIBUTOR") {
                        Alert.updateApp(
                            this,
                            getString(R.string.app_update_by_play)
                        ) { openPlayStore() }
                    }
//                        Alert.updateApp(this, getString(R.string.app_update_by_download)) {
//                            val url = it.app_link
//                            val downloader = DownloadFIle()
//                            downloader.download(this, url)
//                        }
//                    }
//                    else {
//
//                    }
                }
            }

        }

    }*/

    private fun openPlayStore() {
        val market = "market://details?id=$packageName"
        val app_url = "https://play.google.com/store/apps/details?id=$packageName"
        try {
            playLauncher.launch(Intent(Intent.ACTION_VIEW, Uri.parse(market)))
        } catch (e: ActivityNotFoundException) {
            playLauncher.launch(Intent(Intent.ACTION_VIEW, Uri.parse(app_url)))
        }
    }

    val playLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            getAppVersion()
        }

}
const val AUTH_RECEIVER = "AUTH_RECEIVER"
const val LOGOUT_RECEIVER = "LOGOUT_RECEIVER"
class MainModel(application: Application) : AndroidViewModel(application) {
    val bottomAdapter = HomeAdapter(this, 1)
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()


    init {
        getUser()
        var arrayList = ArrayList<BottomMenu>()
        val json = getStringAssets("menu_dist.json")
        if (json!!.isNotEmpty()) {
            arrayList = gson.fromJson(json, object : TypeToken<ArrayList<BottomMenu>>() {}.type)
        }
        bottomAdapter.selected = 1
        bottomAdapter.updateAdapter(arrayList as ArrayList<Any>)
    }

    fun save(user: User) {
        sharedPreference.putString(user_data, gson.toJson(user))
    }

    fun setLatLong(lat: Double, lng: Double) {
        sharedPreference.putString(latitude, lat.toString())
        sharedPreference.putString(longitude, lng.toString())
        Log.d(TAG, "Lat-Long " + lat + "," + lng)

    }

    fun unBloackStatus(result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getUnblockStatus(param).body()!!
                }
            }.onSuccess {
                result.invoke(it)
                isLoaderVisible.value = false
            }.onFailure {
                retrofitError.postValue(errorRetrofit(it))
                isLoaderVisible.value = false
            }
        }
    }

    fun saveFirebaseToken(param:JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().saveFcmToken(param).body()!!
                }
            }.onSuccess {
                result.invoke(it)
                isLoaderVisible.value = false
            }.onFailure {
                retrofitError.postValue(errorRetrofit(it))
                isLoaderVisible.value = false
            }
        }
    }

    fun getBlockStatus(result: (ApiResponse1) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getBlockStatus(param).body()!!
                }
            }.onSuccess {
                result.invoke(it)
                isLoaderVisible.value = false
            }.onFailure {
                retrofitError.postValue(errorRetrofit(it))
                isLoaderVisible.value = false
            }
        }
    }

    fun getUser(): User? {
        val user = sharedPreference.getUserDist()
        return user
    }


    fun uploadprofileimg(base64: String, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("profileimg", base64)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().uploadprofileimg(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                result.invoke(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun getAppVersion(result: (AppUpdateResult) -> Unit) {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("apptype", APP_TYPE)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().gstAppUpdate(param).body()!!
                }
            }.onSuccess {
                result.invoke(it)
            }.onFailure {

            }
        }
    }

}




