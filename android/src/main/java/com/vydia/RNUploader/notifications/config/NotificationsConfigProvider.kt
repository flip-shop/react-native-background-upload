package com.vydia.RNUploader.notifications.config

import com.facebook.react.bridge.ReadableMap

interface NotificationsConfigProvider {

    fun provide(
        options: ReadableMap,
        optionsObtained: (NotificationsConfig) -> Unit = {},
        errorObtained: (String) -> Unit = {}
    )

}