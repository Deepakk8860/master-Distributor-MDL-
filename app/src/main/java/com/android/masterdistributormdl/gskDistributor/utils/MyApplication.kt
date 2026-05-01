package com.android.masterdistributormdl.gskDistributor.utils

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.core.os.bundleOf
import com.android.masterdistributormdl.utils.SharedPreference

class MyApplication : Application(), AppLifecycleHandler.LifeCycleDelegate {
    var isExpire = false
    lateinit var sharedPreference: SharedPreference

    companion object {
        private lateinit var instance: MyApplication
        fun getMyApplication(): Application {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
//        MultiDex.install(this)
        instance = this
        sharedPreference = SharedPreference()
        val lifeCycleHandler = AppLifecycleHandler(this)
        registerLifecycleHandler(lifeCycleHandler)
    }

    override fun onActivityResumed(activity: Activity) {
        Log.e("AppLifecycle", "onActivityResumed")
        TimerUtil.stopTimer()
        val isLogin = sharedPreference.getBoolean(is_login)
        if (isExpire && isLogin && !is_pin_open) {
            isExpire = false
            val bundle = bundleOf("type" to "timeout")
//            if (activity is FragmentActivity) addFragment(activity, LoginPin(), bundle)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        Log.e("AppLifecycle", "onActivityPaused")
        val isLogin = sharedPreference.getBoolean(is_login)
        if (isLogin && !is_pin_open)
            startTimer()
    }

    private fun startTimer() {
        TimerUtil.startTimer(object : TimerListener {
            override fun timerExpire() {
                isExpire = true
            }
        })
    }

    private fun registerLifecycleHandler(lifeCycleHandler: AppLifecycleHandler) {
        registerActivityLifecycleCallbacks(lifeCycleHandler)
        registerComponentCallbacks(lifeCycleHandler)
    }


}