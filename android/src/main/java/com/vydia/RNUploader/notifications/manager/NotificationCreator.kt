package com.vydia.RNUploader.notifications.manager

import com.vydia.RNUploader.notifications.config.NotificationsConfig
import com.vydia.RNUploader.notifications.data.NotificationType

interface NotificationCreator {
    fun displayNotification(
        notificationType: NotificationType,
        notificationsConfig: NotificationsConfig
    )

    fun removeNotification(type: NotificationType)
}