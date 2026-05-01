package com.android.masterdistributormdl.gskDistributor.services

import android.content.Intent
import android.util.Log
import androidx.annotation.Keep
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.pushwoosh.Pushwoosh
import com.pushwoosh.notification.NotificationServiceExtension
import com.pushwoosh.notification.PushMessage

@Keep
class ServiceExtension : NotificationServiceExtension() {
    override fun startActivityForPushMessage(pushMessage: PushMessage) {
        //   super.startActivityForPushMessage(pushMessage)
        try {
            val message = pushMessage.toJson().toString()
            Log.d(TAG, "PushWooshMessage $message")
            val context = applicationContext ?: return
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra(Pushwoosh.PUSH_RECEIVE_EVENT, message)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun isAppOnForeground(): Boolean {
        return super.isAppOnForeground()
    }
}