package com.vydia.RNUploader.notifications.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.RemoteException
import com.vydia.RNUploader.defaultNotificationChannelId
import com.vydia.RNUploader.defaultNotificationChannelName

class NotificationChannelManagerImpl(
    private val context: Context
): NotificationChannelManager {

    override fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = (context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as? NotificationManager)

            try {
                notificationManager?.createNotificationChannel(
                    NotificationChannel(
                        defaultNotificationChannelId,
                        defaultNotificationChannelName,
                        NotificationManager.IMPORTANCE_LOW
                    )
                )
            } catch (e: RemoteException) {
                //
            }
        }
    }

    override fun deleteChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try{
                (context.getSystemService(
                    Context.NOTIFICATION_SERVICE) as? NotificationManager
                )?.deleteNotificationChannel(defaultNotificationChannelId)
            }catch (e: RemoteException) {
                //
            }
        }
    }
}