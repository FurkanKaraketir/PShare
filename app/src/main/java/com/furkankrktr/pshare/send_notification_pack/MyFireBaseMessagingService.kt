package com.furkankrktr.pshare.send_notification_pack

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import com.furkankrktr.pshare.CommentsActivity
import com.furkankrktr.pshare.HaberlerActivity
import com.furkankrktr.pshare.R
import com.furkankrktr.pshare.RepliesActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@Suppress("DEPRECATION")
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFireBaseMessagingService : FirebaseMessagingService() {
    private lateinit var title: String
    private lateinit var message: String
    private lateinit var intentID: String
    private lateinit var intentEmail: String
    private lateinit var resultIntent: Intent
    private lateinit var intentText: String
    private lateinit var intentImage: String
    private lateinit var intentUID: String
    private lateinit var selectedPostUID: String
    override fun onMessageReceived(@NonNull remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        title = remoteMessage.data["Title"].toString()
        message = remoteMessage.data["Message"].toString()
        intentID = remoteMessage.data["IntentID"].toString()
        intentEmail = remoteMessage.data["IntentEmail"].toString()
        intentText = remoteMessage.data["IntentText"].toString()
        intentImage = remoteMessage.data["selectedCommentImage"].toString()
        intentUID = remoteMessage.data["selectedCommentUID"].toString()
        selectedPostUID = remoteMessage.data["selectedPostUID"].toString()

        when (title) {
            "Postunuza Yeni Yorum" -> {
                resultIntent = Intent(this, CommentsActivity::class.java)
                resultIntent.putExtra("selectedPost", intentID)
                resultIntent.putExtra("selectedPostEmail", intentEmail)
                resultIntent.putExtra("selectedPostUID", selectedPostUID)
                resultIntent.putExtra("selectedPostImage", intentImage)
                resultIntent.putExtra("selectedPostText", intentText)

            }
            "Yorumunuza Yeni Yanıt" -> {
                resultIntent = Intent(this, RepliesActivity::class.java)
                resultIntent.putExtra("selectedComment", intentID)
                resultIntent.putExtra("selectedCommentEmail", intentEmail)
                resultIntent.putExtra("selectedCommentText", intentText)
                resultIntent.putExtra("selectedCommentImage", intentImage)
                resultIntent.putExtra("selectedCommentUID", intentUID)
            }
            else -> {
                resultIntent = Intent(this, HaberlerActivity::class.java)
            }
        }

        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(50, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Bildirimler"
            val descriptionText = "Yorum ve Yanıt Bildirimleri"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(212121.toString(), name, importance)
            mChannel.description = descriptionText
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        val builder = NotificationCompat.Builder(applicationContext)
            .setSmallIcon(R.drawable.ic_baseline_attach_file_24)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(Color.argb(100, 105, 0, 225))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(0, builder.build())
    }
}