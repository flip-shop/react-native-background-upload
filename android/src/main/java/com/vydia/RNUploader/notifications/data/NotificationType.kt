package com.vydia.RNUploader.notifications.data

import com.vydia.RNUploader.helpers.notificationCancelledId
import com.vydia.RNUploader.helpers.notificationErrorId
import com.vydia.RNUploader.helpers.notificationUploadProgressId

sealed class NotificationType(val id: Int) {
    class Progress(
        val progress: Int = 0,
        val uploadId: String? = null
    ): NotificationType(notificationUploadProgressId)
    object Canceled: NotificationType(notificationCancelledId)
    object Error: NotificationType(notificationErrorId)
}
