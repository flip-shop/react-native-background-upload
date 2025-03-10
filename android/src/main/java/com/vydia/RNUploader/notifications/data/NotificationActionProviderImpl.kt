package com.vydia.RNUploader.notifications.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vydia.RNUploader.notifications.NotificationActionsReceiver
import com.vydia.RNUploader.helpers.cancelUploadBroadcastRequestCode

//const val ACTION_CANCEL_UPLOAD = "cancelUpload"
const val ACTION_UPLOAD_CANCEL = "com.vydia.RNUploader.notifications.data.cancelUploadAction"
const val PARAM_UPLOAD_ID = "uploadId"

class NotificationActionProviderImpl(
    private val context: Context
): NotificationActionProvider {

    override fun provideCancelUploadAction(uploadId: String): PendingIntent {

        val intent = Intent(context, NotificationActionsReceiver::class.java)
        intent.action = ACTION_UPLOAD_CANCEL
        intent.putExtra(PARAM_UPLOAD_ID, uploadId)

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(
            context,
            cancelUploadBroadcastRequestCode,
            intent,
            pendingIntentFlags
        )
    }
}