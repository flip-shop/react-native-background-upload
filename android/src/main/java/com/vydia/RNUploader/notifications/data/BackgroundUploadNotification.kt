package com.vydia.RNUploader.notifications.data

data class BackgroundUploadNotification(
    val type: NotificationType,
    val title: String?,
    val body: String
) {
}