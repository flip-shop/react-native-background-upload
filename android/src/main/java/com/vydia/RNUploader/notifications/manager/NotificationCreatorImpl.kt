package com.vydia.RNUploader.notifications.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vydia.RNUploader.defaultNotificationChannelId
import com.vydia.RNUploader.maxUploadProgress
import com.vydia.RNUploader.notifications.config.NotificationsConfig
import com.vydia.RNUploader.notifications.data.BackgroundUploadNotification
import com.vydia.RNUploader.notifications.data.NotificationType

class NotificationCreatorImpl(
    private val context: Context
): NotificationCreator {

    override fun displayNotification(
        notification: BackgroundUploadNotification,
        notificationsConfig: NotificationsConfig
    ) {
        val notificationBuilder =  NotificationCompat.Builder(context, defaultNotificationChannelId)

        notificationBuilder.setContentTitle(notification.title)
        notificationBuilder.setAutoCancel(notificationsConfig.autoClear)
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload)

        when(notification.type) {
            NotificationType.Error,
            NotificationType.Canceled -> notificationBuilder.setContentText(notification.body)
            NotificationType.Progress -> notificationBuilder.setProgress(
                    maxUploadProgress,
                    notification.progress,
                    false
                )
        }

        if (ActivityCompat.checkSelfPermission(
                context,
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


        NotificationManagerCompat.from(context).notify(
            notification.type.id,
            notificationBuilder.build()
        )
    }

    override fun removeNotification(type: NotificationType) {
        NotificationManagerCompat.from(context).cancel(type.id)
    }


}