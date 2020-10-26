package com.furkankrktr.pshare.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.furkankrktr.pshare.HaberlerActivity
import com.furkankrktr.pshare.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        remoteMessage.notification?.let {

            val intent = Intent(this, HaberlerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 5, intent, 0)


            val builder = it.channelId?.let { it1 ->
                NotificationCompat.Builder(this, it1)
                    .setSmallIcon(R.drawable.ic_photo_black)
                    .setContentTitle(it.title)
                    .setContentText(it.body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
            }
            createNotificationChannel(it)
            with(NotificationManagerCompat.from(this)) {

                if (builder != null) {
                    notify(it.channelId.toString().toInt(), builder.build())
                }
            }

        }

    }


    private fun createNotificationChannel(it: RemoteMessage.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mesaj"
            val descriptionText = "Fazla uğraşma buralarla"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(it.channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}