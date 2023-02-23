package com.vydia.RNUploader.notifications

import com.facebook.react.bridge.ReadableMap

interface NotificationsConfigProvider {

    fun provide(
        options: ReadableMap,
        optionsObtained: (NotificationsConfig) -> Unit = {},
        errorObtained: (String) -> Unit = {}
    )

}