package com.example.notification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.notification.Utils.BROADCAST_PERMISSION_ACTION
import com.example.notification.Utils.NOTIFICATION_ID


class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.action?.let {
            when {
                it == BROADCAST_PERMISSION_ACTION -> {
                    // Dismiss the notification
                    context?.let { NotificationManagerCompat.from(it).cancel(NOTIFICATION_ID) }
                }

                else -> Unit
            }
        }
    }
}