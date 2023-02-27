package com.vydia.RNUploader.notifications

import com.facebook.react.bridge.ReadableMap

class NotificationsConfigProviderImpl: NotificationsConfigProvider {

    override fun provide(
        options: ReadableMap,
        optionsObtained: (NotificationsConfig) -> Unit,
        errorObtained: (String) -> Unit
    ) {
        val config = NotificationsConfig()


    }
}