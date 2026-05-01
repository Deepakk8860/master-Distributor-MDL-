package com.android.masterdistributormdl.firebase
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your server if needed
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("fjgjjgfff", "onMessageReceived: $remoteMessage")
        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            val data = remoteMessage.data

            val url = data["url"]
            val event = data["event"]
            val title = data["title"] ?: "Title"
            val message = data["body"] ?: "Message"
            val imageUrl = data["imageUrl"]

            NotificationHelper(applicationContext).createNotification(title, message, url, event)
        }


        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            val title = it.title ?: "Title"
            val message = it.body ?: "Message"
            val imageUrl = it.imageUrl.toString()
            Log.d("fjgjjgfff", "onMessageReceived: $title")

            NotificationHelper(applicationContext).createNotification(title, message, imageUrl)
        }
    }

}