package com.vydia.RNUploader.notifications

const val defaultChannelName = "BackgroundUploadNotificationChannel"
data class NotificationsConfig(
    var enabled: Boolean = true,
    var notificationChannel: String = defaultChannelName,
    var progressNotification: BackgroundUploadNotification? = null,
    var successNotification: BackgroundUploadNotification? = null,
    var cancelNotification: BackgroundUploadNotification? = null,
    var errorNotification: BackgroundUploadNotification? = null
)