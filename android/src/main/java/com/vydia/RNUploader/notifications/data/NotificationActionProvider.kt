package com.vydia.RNUploader.notifications.data

import android.app.PendingIntent

interface NotificationActionProvider {
    fun provideCancelUploadAction(uploadId: String): PendingIntent
}