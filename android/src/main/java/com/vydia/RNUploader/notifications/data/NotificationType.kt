package com.vydia.RNUploader.notifications.data

import com.vydia.RNUploader.notificationCancelledId
import com.vydia.RNUploader.notificationErrorId
import com.vydia.RNUploader.notificationUploadProgressId

sealed class NotificationType(val id: Int) {
    object Progress: NotificationType(notificationUploadProgressId)
    object Canceled: NotificationType(notificationCancelledId)
    object Error: NotificationType(notificationErrorId)
}
