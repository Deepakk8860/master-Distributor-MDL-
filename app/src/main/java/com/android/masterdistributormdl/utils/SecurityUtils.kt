package com.android.masterdistributormdl.utils

import android.app.Activity
import android.view.WindowManager
import com.android.masterdistributormdl.utils.ROLE
import com.android.masterdistributormdl.utils.sharedPreference

object SecurityUtils {
    fun applySecureFlag(activity: Activity) {
//        Log.d("fgurbgburgu", "i am security: ${sharedPreference.getString(ROLE)}")

        if (sharedPreference.getString(ROLE) != "admin") {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }else{
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
