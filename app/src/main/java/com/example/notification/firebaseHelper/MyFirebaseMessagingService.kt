package com.example.notification.firebaseHelper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.notification.MainActivity
import com.example.notification.R
import com.example.notification.Utils.ANSWER_TEXT
import com.example.notification.Utils.BROADCAST_PERMISSION_ACTION
import com.example.notification.Utils.CHANNEL_ID
import com.example.notification.Utils.DECLINE_TEXT
import com.example.notification.Utils.NOTIFICATION_ID
import com.example.notification.receiver.ActionReceiver
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = MyFirebaseMessagingService::class.java.name
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a data payload.
        var title: String? = null
        var body: String? = null
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            title = remoteMessage.data["title"]
            body = remoteMessage.data["body"]
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            title = it.title
            body = it.body
        }
        title?.let { createNotification(it, body) }
    }

    private fun createNotification(it: String, body: String?) {
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID) // cancel the previous notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                it,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = body
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent for the Reject action button
        val actionPendingAction = getRejectPendingAction()

        // adding action for Answer CTA -> activity
        val activityActionPendingAction = getAnswerPendingAction()

        // Create a NotificationCompat.Builder and set the notification content
        val builder =
            createNotificationBuilder(it,body, activityActionPendingAction, actionPendingAction)

        // Show the notification
        showNotification(builder, notificationManager)
    }

    private fun showNotification(
        builder: NotificationCompat.Builder,
        notificationManager: NotificationManager,
    ) {
        with(notificationManager) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun getAnswerPendingAction(): NotificationCompat.Action {
        val activityActionIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val activityActionPendingIntent: PendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                activityActionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )
        return getAction(activityActionPendingIntent, ANSWER_TEXT)
    }

    private fun getRejectPendingAction(): NotificationCompat.Action {
        val actionIntent = Intent(this, ActionReceiver::class.java)
        actionIntent.setAction(BROADCAST_PERMISSION_ACTION)

        val actionPendingIntent =
            PendingIntent.getBroadcast(
                this,
                0,
                actionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )
        return getAction(actionPendingIntent, DECLINE_TEXT)
    }

    private fun getAction(actionPendingIntent: PendingIntent?, ctaName: String) =
        NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher_round,
            ctaName,
            actionPendingIntent
        ).build()

    private fun createNotificationBuilder(
        it: String,
        body: String?,
        activityActionPendingAction: NotificationCompat.Action,
        actionPendingAction: NotificationCompat.Action
    ): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(it)
            .setContentText(body)
            .addAction(actionPendingAction/*getRejectPendingAction()*/)
            .addAction(activityActionPendingAction/*getAnswerPendingAction()*/)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        return builder
    }

}