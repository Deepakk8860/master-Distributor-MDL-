package com.android.masterdistributormdl.main

import android.app.Application
import android.app.Fragment
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.masterdistributormdl.BuildConfig
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.adapter.HomeAdapter
import com.android.masterdistributormdl.addLead.AddLead
import com.android.masterdistributormdl.addLead.ManageLead
import com.android.masterdistributormdl.databinding.MainActivityBinding
import com.android.masterdistributormdl.doc.DocSharing
import com.android.masterdistributormdl.follow_up.FollowUpLead
import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.APP_TYPE
import com.android.masterdistributormdl.gskDistributor.utils.Alert
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.LOGOUT_RECEIVER
import com.android.masterdistributormdl.gskDistributor.view.MainActivity.LogoutReceiver
import com.android.masterdistributormdl.home.Home
import com.android.masterdistributormdl.login.Login
import com.android.masterdistributormdl.model.BottomMenu
import com.android.masterdistributormdl.model.User
import com.android.masterdistributormdl.profile.Profile
import com.android.masterdistributormdl.utils.MyApplication.Companion.logoutAlreadyHandled
import com.android.masterdistributormdl.utils.STATUS_COLOR2
import com.android.masterdistributormdl.utils.SharedPreference
import com.android.masterdistributormdl.utils.getAppFragmentManager
import com.android.masterdistributormdl.utils.getDialog
import com.android.masterdistributormdl.utils.getStringAssets
import com.android.masterdistributormdl.utils.gson
import com.android.masterdistributormdl.utils.replaceFragment
import com.android.masterdistributormdl.utils.showToast
import com.android.masterdistributormdl.utils.user_data
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.gsk.distributor.model.AppUpdateResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


const val FCM_SENDER_ID = "172767290696"
const val PUSH_APP_ID = "83B42-2DD16"

class MainActivity : FragmentActivity() {
    private lateinit var binding: MainActivityBinding
    lateinit var model: MainModel
    private lateinit var logoutReceiver: LogoutReceiver
    private var isTabPage = true
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this)[MainModel::class.java]
        binding = MainActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        initView()
        if (model.user != null){
            getAppVersion()
        }

        initListener()
        setFragment()
    }


    private fun initView() {
        // Initialize Google Sign-In Client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initListener() {
        model.bottomAdapter.setOnclickListener {
            it as BottomMenu
            if (it.id == 1) {
                replaceFragment(this, Home())
            } else if (it.id == 2) {
                replaceFragment(this, AddLead())
            } else if (it.id == 3) {
                replaceFragment(this, FollowUpLead())
            } else if (it.id == 4) {
                replaceFragment(this, ManageLead())
            } else if (it.id == 5) {
                replaceFragment(this, DocSharing())
            }

        }
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
            if (model.user != null) {
                if (logoutAlreadyHandled) return
                logoutAlreadyHandled = true
                clearAppData()
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


    private fun setFragment() {
        if (model.user == null) {
            replaceFragment(
                this, Login()
            )
        } else {
            replaceFragment(this, Home())
        }
    }

    /* override fun onBackPressed() {
         if (model.user == null) {
             super.onBackPressed()
             return
         }
         val selected = model.bottomAdapter.selected
         val count = getAppFragmentManager(this).backStackEntryCount
         if (selected == 1 && count == 0) {
             appCloseDialog()
         } else if (selected != 1 && count == 0) {
             updateBottom(1,Home())
         } else {
             super.onBackPressed()
         }
     }*/

    override fun onBackPressed() {
        if (model.user == null) {
            super.onBackPressed()
            return
        }
        val selected = model.bottomAdapter.selected
        val count = getAppFragmentManager(this).backStackEntryCount
        if (selected == 1 && count == 0) {
            if (model.user == null) {
                appCloseDialog()
            } else {
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.slide_down)
            }
        } else if (selected != 1 && count == 0) {
            setHome()
        } else {
            super.onBackPressed()
            return
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

    private fun setHome() {
        model.bottomAdapter.selected = 1
        model.bottomAdapter.updateAdapter()
        replaceFragment(this, Home())
    }

    fun updateBottom(index: Int, fragment: androidx.fragment.app.Fragment) {
        model.bottomAdapter.selected = index
        model.bottomAdapter.updateAdapter()
        replaceFragment(this, fragment)
    }

    fun openBottomSheetAlert() {
        val dialog = BottomSheetDialog(this, R.style.TransparentBottomSheet)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_email_prompt, null)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }


        dialog.setContentView(view)

        dialog.show()

        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            dialog.dismiss()
        }

    }


    override fun onResume() {
        super.onResume()
        startLogoutReceiver()
        startAuthReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterLogoutReceiver()
        unRegisterAuthReceiver()
    }


    fun setHeader(title: String, color: String) {
        isTabPage = false
        binding.bottomView.visibility = View.GONE
        statusBarColor(color)
    }

    fun setHeader2(title: String, color: String) {
        isTabPage = true
        binding.bottomView.visibility = View.VISIBLE
        val padding = resources.getDimensionPixelOffset(R.dimen._48sp)
        statusBarColor(color)
    }

    private fun statusBarColor(color: String) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor(color)
        val isLight = color != STATUS_COLOR2
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
    }

    private lateinit var authtReceiver: AuthReceiver


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

    fun logoutDialog() {
        val dialog = getDialog(this, R.layout.logout_alert)
        val logout = dialog.findViewById<Button>(R.id.logout)
        val cancel = dialog.findViewById<Button>(R.id.cancel)
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        logout.setOnClickListener {
            dialog.dismiss()
            clearAppData()

        }
        dialog.show()

    }

    private fun clearAppData() {
        model.sharedPreference.clearSharedPrefernce()
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInClient.revokeAccess().addOnCompleteListener {
                // ✅ Successfully signed out and access revoked
            }
        }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getAppVersion() {
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


    inner class AuthReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status = intent.getStringExtra("status")
            if (status == "auth") {
                showToast("Your session has expired, please login again!!")
                val intent2 = Intent(context, SplashActivity::class.java)
                intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent2)
                finish()
            } else if (status == "error") {
                val intent2 = Intent(context, ErrorActivity::class.java)
                intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent2)
                finish()
            }
        }
    }
}

const val NOTI_COUNT = "NOTI_COUNT"
const val AUTH_RECEIVER = "AUTH_RECEIVER"
const val PLAN_RECEIVER = "PLAN_RECEIVER"

class MainModel(application: Application) : AndroidViewModel(application) {
    val bottomAdapter = HomeAdapter(this, 1)
    val sharedPreference = SharedPreference()
    var user = sharedPreference.getUser()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
//    val retrofitError = MutableLiveData<ErrorAlert>()

    var notiCount = sharedPreference.getInteger(NOTI_COUNT)

    init {
        var arrayList = ArrayList<BottomMenu>()
        val json = getStringAssets("menu.json")
        if (json!!.isNotEmpty()) {
            arrayList = gson.fromJson(json, object : TypeToken<ArrayList<BottomMenu>>() {}.type)
        }
        bottomAdapter.selected = 1
        bottomAdapter.updateAdapter(arrayList as ArrayList<Any>)
    }

    fun save(user: User) {
        this.user = user
        sharedPreference.putString(user_data, gson.toJson(user))
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




