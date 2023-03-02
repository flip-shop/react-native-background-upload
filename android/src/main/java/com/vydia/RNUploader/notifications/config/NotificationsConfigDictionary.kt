package com.vydia.RNUploader.notifications.config

const val notificationsMapKey = "notification"
const val enabledKey = "enabled"
const val autoClearKey = "autoClear"
const val onProgressTitleKey = "onProgressTitle"
const val onErrorTitleKey = "onErrorTitle"
const val onErrorMessageKey = "onErrorMessage"
const val onCancelledTitleKey = "onCancelledTitle"
const val onCancelledMessageKey = "onCancelledMessage"

val notificationConfigKeySet = arrayOf(
    autoClearKey,
    onProgressTitleKey,
    onErrorTitleKey,
    enabledKey,
    onErrorMessageKey,
    onCancelledTitleKey,
    onCancelledMessageKey
)