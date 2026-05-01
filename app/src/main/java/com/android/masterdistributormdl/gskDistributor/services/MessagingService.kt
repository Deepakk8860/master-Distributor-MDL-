package com.android.masterdistributormdl.gskDistributor.services

/*
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gsk.franchise.R
import com.gsk.franchise.utils.SharedPreference
import com.gsk.franchise.utils.device_token
import com.gsk.franchise.utils.is_open
import com.gsk.franchise.view.MainActivity
import java.net.URL
import kotlin.random.Random


class MessagingService : FirebaseMessagingService() {
    val sharedPreference = SharedPreference()
    var title: String? = null
    var body: String? = null
    var type: String? = null
    var image: String? = null

    var isCancelable = true
    override fun onMessageReceived(data: RemoteMessage) {
        super.onMessageReceived(data)
        Log.d(Constants.TAG, "MessagingService")
        val notification = data.notification
        val map = data.data
        var isCheck = false
        if (!map.isNullOrEmpty()) {
            isCheck = true
            image = map.get("image")
            title = map.get("title")
            body = map.get("body")
            type = if (map.get("type").isNullOrEmpty()) "alert" else map.get("type")

        } else if (notification != null) {
            isCheck = true
            title = notification.title
            body = notification.body
            type = "alert"
            image = notification.imageUrl.toString()
        }
        if (isCheck) {
            if (image.isNullOrEmpty()) {
                notificationOrders(null)
            } else {
                getBitmapFromUrl()
            }
        }

    }

    override fun onNewToken(token: String) {
        sharedPreference.putString(device_token, token)
    }

    fun getBitmapFromUrl() {
        try {
            val inputStream = URL(image).openStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            notificationOrders(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            notificationOrders(null)
        }
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    private fun notificationOrders(bitmap: Bitmap?) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val random = Random.nextInt(1000)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(is_open, 2)
        intent.putExtra("title", title)
        intent.putExtra("body", body)
        intent.putExtra("type", type)

        val vibrationPattern = longArrayOf(500, 500)

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getActivity(this, random, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        } else {
            getActivity(this, random, intent, FLAG_ONE_SHOT or FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("0", title, importance)
            channel.description = body
            channel.enableVibration(true)
            channel.enableLights(true)
            channel.vibrationPattern = vibrationPattern
            notificationManager.createNotificationChannel(channel)
        }
        val icon = BitmapFactory.decodeResource(getResources(), R.drawable.logo_circle)
        val builder = NotificationCompat.Builder(this, "0")
        builder.setContentTitle(title)
            .setSmallIcon(R.drawable.small_logo)
            .setLargeIcon(icon)
            .setContentText(body)
            .setSound(soundUri)
            .setVibrate(vibrationPattern)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(Notification.DEFAULT_ALL)
            .setColor(ContextCompat.getColor(this, R.color.trans))
            .setAutoCancel(isCancelable)
            .setOngoing(false)
            .setContentIntent(pendingIntent)
        if (bitmap != null)
            builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
        notificationManager.notify(random, builder.build())

    }
}
*/
