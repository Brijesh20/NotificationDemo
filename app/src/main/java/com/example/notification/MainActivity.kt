package com.example.notification

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationManagerCompat
import com.example.notification.Utils.NOTIFICATION_ID
import com.example.notification.ui.compose.NotificationPermissionRequest
import com.example.notification.ui.theme.NotificationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        setContent {
            NotificationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    NotificationPermissionRequest()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotificationTheme {
    }
}