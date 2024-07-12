package com.example.notification.firebaseHelper

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.notification.MainActivity
import com.example.notification.R
import com.example.notification.Utils.BROADCAST_PERMISSION_ACTION
import com.example.notification.Utils.CHANNEL_ID
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
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            createNotification(it)
        }
    }

    private fun createNotification(it: RemoteMessage.Notification) {
        // Register the channel with the system
        val notificationManager: NotificationManager =
            application.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                it.title,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = it.body
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent for the Reject action button
        val actionPendingAction = getRejectPendingAction()

        // adding action for Answer CTA -> activity
        val activityActionPendingAction = getAnswerPendingAction()

        // Create a NotificationCompat.Builder and set the notification content
        val builder = createNotificationBuilder(it, activityActionPendingAction, actionPendingAction)

        // Show the notification
        showNotification(builder)
    }

    private fun showNotification(builder: NotificationCompat.Builder) {
        with(NotificationManagerCompat.from(application)) {
            if (ActivityCompat.checkSelfPermission(
                    application,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun getAnswerPendingAction(): NotificationCompat.Action {
        val activityActionIntent = Intent(application, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val activityActionPendingIntent: PendingIntent =
            PendingIntent.getActivity(
                application,
                0,
                activityActionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )
        return getAction(activityActionPendingIntent, "Answer")
    }

    private fun getRejectPendingAction(): NotificationCompat.Action {
        val actionIntent = Intent(application, ActionReceiver::class.java)
        actionIntent.setAction(BROADCAST_PERMISSION_ACTION)

        val actionPendingIntent =
            PendingIntent.getBroadcast(
                application,
                0,
                actionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )
        return getAction(actionPendingIntent, "Decline")
    }

    private fun getAction(actionPendingIntent: PendingIntent?, ctaName: String) =
        NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher_round,
            ctaName,
            actionPendingIntent
        ).build()

    private fun createNotificationBuilder(
        it: RemoteMessage.Notification,
        activityActionPendingAction: NotificationCompat.Action,
        actionPendingAction: NotificationCompat.Action
    ): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(application, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(it.title)
            .setContentText(it.body)
            .addAction(actionPendingAction/*getRejectPendingAction()*/)
            .addAction(activityActionPendingAction/*getAnswerPendingAction()*/)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        return builder
    }

}