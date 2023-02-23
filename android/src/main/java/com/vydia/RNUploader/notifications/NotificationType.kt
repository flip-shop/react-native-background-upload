package com.vydia.RNUploader.notifications

sealed class NotificationType {
    class Progress(val progress: Int): NotificationType()
    object Success: NotificationType()
    object Canceled: NotificationType()
    object Error: NotificationType()
}
