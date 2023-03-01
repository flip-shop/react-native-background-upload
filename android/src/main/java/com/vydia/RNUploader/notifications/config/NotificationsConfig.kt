package com.vydia.RNUploader.notifications.config

import com.vydia.RNUploader.defaultNotificationChannelName
import com.vydia.RNUploader.emptyString



data class NotificationsConfig(
    var enabled: Boolean = true,
    var autoClear: Boolean = true,
    var notificationChannel: String? = defaultNotificationChannelName,
    var onProgressTitle: String? = emptyString,
    var onErrorTitle: String? = emptyString,
    var onErrorMessage: String? = emptyString,
    var onCancelledTitle: String? = emptyString,
    var onCancelledMessage: String? = emptyString
)