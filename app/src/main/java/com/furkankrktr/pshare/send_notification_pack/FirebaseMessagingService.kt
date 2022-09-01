@file:Suppress("DEPRECATION")

package com.furkankrktr.pshare.send_notification_pack

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.furkankrktr.pshare.HaberlerActivity
import com.furkankrktr.pshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseMessagingService : FirebaseMessagingService() {
    private lateinit var auth: FirebaseAuth
    private var mNotificationManager: NotificationManager? = null

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        auth = FirebaseAuth.getInstance()


// playing audio and vibration when user se reques
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(applicationContext, notification)
        r.play()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.isLooping = false
        }

        // vibration
        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(100, 300, 300, 300)
        v.vibrate(pattern, -1)
        val builder = NotificationCompat.Builder(this, "212121")
        //            builder.setSmallIcon(R.drawable.icontrans);
        val resultIntent = Intent(this, HaberlerActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentTitle(remoteMessage.notification!!.title)
        builder.setContentText(remoteMessage.notification!!.body)
        builder.setSmallIcon(R.drawable.ic_baseline_attach_file_24)
        builder.color = resources.getColor(R.color.colorPrimary)
        builder.setContentIntent(pendingIntent)
        builder.setStyle(
            NotificationCompat.BigTextStyle().bigText(
                remoteMessage.notification!!.body
            )
        )
        builder.setAutoCancel(true)
        builder.priority = Notification.PRIORITY_MAX
        mNotificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "212121"
        val channel = NotificationChannel(
            channelId,
            "Bildirimler",
            NotificationManager.IMPORTANCE_HIGH
        )
        mNotificationManager!!.createNotificationChannel(channel)
        builder.setChannelId(channelId)


// notificationId is a unique int for each notification that you must define
        mNotificationManager!!.notify(100, builder.build())
    }
}
