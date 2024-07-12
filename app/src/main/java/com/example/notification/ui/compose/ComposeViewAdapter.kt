package com.example.notification.ui.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView

class ComposeViewAdapter(
    private val context: Context,
    private val composable: @Composable () -> Unit
) {
    fun getView() =
        ComposeView(context = context).apply {
            setContent {
                composable()
            }
        }
}