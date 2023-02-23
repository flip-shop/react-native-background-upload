package com.vydia.RNUploader.notifications

data class BackgroundUploadNotification(
    val type: NotificationType,
    val title: String,
    val body: String,
    val autoClear: Boolean
) {
}