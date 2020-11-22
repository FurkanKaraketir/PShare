package com.furkankrktr.pshare.send_notification_pack

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import com.furkankrktr.pshare.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@Suppress("DEPRECATION")
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFireBaseMessagingService : FirebaseMessagingService() {
    private lateinit var title: String
    private lateinit var message: String
    private lateinit var long: LongArray
    override fun onMessageReceived(@NonNull remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        long = longArrayOf(1000, 1000, 1000, 1000, 1000)
        title = remoteMessage.data["Title"].toString()
        message = remoteMessage.data["Message"].toString()
        val builder = NotificationCompat.Builder(applicationContext)
            .setSmallIcon(R.drawable.ic_baseline_attach_file_24)
            .setContentTitle(title)
            .setContentText(message)
            .setVibrate(long)
            .setColor(Color.argb(100, 105, 0, 225))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(0, builder.build())
    }
}