package com.android.masterdistributormdl.firebase

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.bumptech.glide.Glide
import java.util.concurrent.ExecutionException

class NotificationHelper(val context: Context) {

    private val chanelId = "MasterDistributorApp"
    private var notificationIdCounter = 0


    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification(title: String, message: String, url: String? = null, event: String? = null, imageUrl: String? = null) {
        val notificationId = notificationIdCounter++

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("url", url)
            putExtra("event", event)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_ONE_SHOT  or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            chanelId,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)


        val notificationBuilder = NotificationCompat.Builder(context, chanelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.red))

        if (imageUrl != null) {
            try {
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .submit()
                    .get()

                notificationBuilder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                )
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        val notification = notificationBuilder.build()
        notificationManager.notify(0, notification)

        if (isAppInBackground(context)){
            val event2 = intent.getStringExtra("event")
            val url2 = intent.getStringExtra("url")
            if (event2 == "browser" && url2 != null) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                val browserPendingIntent = PendingIntent.getActivity(
                    context, 0, browserIntent,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
                notification.contentIntent = browserPendingIntent
            }
        }
    }
}

private fun isAppInBackground(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    val appProcesses = activityManager?.runningAppProcesses
    appProcesses?.let {
        for (appProcess in appProcesses) {
            if (appProcess.processName == context.packageName) {
                return appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }
    }
    return false
}