package com.vydia.RNUploader.notifications.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vydia.RNUploader.helpers.defaultNotificationChannelId
import com.vydia.RNUploader.helpers.maxUploadProgress
import com.vydia.RNUploader.helpers.notificationProgressCancelActionText
import com.vydia.RNUploader.notifications.config.NotificationsConfig
import com.vydia.RNUploader.notifications.data.NotificationActionProvider
import com.vydia.RNUploader.notifications.data.NotificationActionProviderImpl
import com.vydia.RNUploader.notifications.data.NotificationType
import org.koin.java.KoinJavaComponent.inject

class NotificationCreatorImpl(
    private val context: Context
): NotificationCreator {

    private val notificationActionProvider: NotificationActionProvider
        by inject(NotificationActionProviderImpl::class.java)

    override fun displayNotification(
        notificationType: NotificationType,
        notificationsConfig: NotificationsConfig
    ) {

        //
        val notificationBuilder = NotificationCompat.Builder(context, defaultNotificationChannelId)

        //
        val title = when(notificationType) {
            NotificationType.Error -> notificationsConfig.onErrorTitle
            NotificationType.Canceled -> notificationsConfig.onCancelledTitle
            is NotificationType.Progress -> notificationsConfig.onProgressTitle
        }

        //
        val message = when(notificationType) {
            NotificationType.Error -> notificationsConfig.onErrorMessage
            NotificationType.Canceled -> notificationsConfig.onCancelledMessage
            is NotificationType.Progress -> null
        }

        //
        notificationBuilder.setContentTitle(title)
        message?.let { notificationBuilder.setContentText(it) }
        notificationBuilder.setAutoCancel(notificationsConfig.autoClear)
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload)


        if(notificationType is NotificationType.Progress) {
            notificationBuilder.setProgress(
                maxUploadProgress,
                notificationType.progress,
                false
            )

            notificationType.uploadId?.let { uploadId ->
                notificationBuilder.addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    notificationProgressCancelActionText,
                    notificationActionProvider.provideCancelUploadAction(uploadId)
                )
            }
        }

        //
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            //
            return
        }

        //
        NotificationManagerCompat.from(context).notify(
            notificationType.id,
            notificationBuilder.build()
        )
    }

    override fun removeNotification(type: NotificationType) {
        NotificationManagerCompat.from(context).cancel(type.id)
    }

}