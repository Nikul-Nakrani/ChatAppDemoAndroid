package com.example.simplechatappdemo



import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.simplechatappdemo.ui.ChatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseMsgService : FirebaseMessagingService() {

    //show the notification
    @SuppressLint("MissingPermission")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (message.data.isNotEmpty()) {
            val senderName = message.data["senderName"]
            val senderUid = message.data["senderUid"]

            // Create an intent to open the ChatActivity
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("name", senderName)
            Log.d("TAG", "onMessageReceived: $senderName")
            intent.putExtra("uId", senderUid)
            Log.d("TAG", "onMessageReceived:$senderUid ")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            // Create a PendingIntent
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            // Create a Notification Channel for Android Oreo and above
            createNotificationChannel()

            // Build the notification
            val builder =
                NotificationCompat.Builder(this, "channel_id").setContentTitle("New Message")
                    .setContentText("You have a new message from $senderName")
                    .setSmallIcon(R.drawable.ic_notication).setOnlyAlertOnce(true)
                    .setContentIntent(pendingIntent).setVibrate(longArrayOf(1000, 1000, 1000, 1000))
                    .setAutoCancel(true)

            // Issue the notification
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(0, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel Name"
            val descriptionText = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel("channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}