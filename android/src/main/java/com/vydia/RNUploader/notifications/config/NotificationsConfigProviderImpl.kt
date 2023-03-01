package com.vydia.RNUploader.notifications.config

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.vydia.RNUploader.ReadableMapFieldState
import com.vydia.RNUploader.keyWrongTypeMessage
import com.vydia.RNUploader.missingKeyMessage
import com.vydia.RNUploader.obtainFieldState

class NotificationsConfigProviderImpl: NotificationsConfigProvider {

    override fun provide(
        options: ReadableMap,
        optionsObtained: (NotificationsConfig) -> Unit,
        errorObtained: (String) -> Unit
    ) {
        val notificationsConfig = NotificationsConfig()

        for(key in notificationConfigKeySet) {
            when(
                obtainFieldState(
                    map = options,
                    fieldNameKey = key,
                    requiredFieldType = when(key) {
                        enabledKey, autoClearKey -> ReadableType.Boolean
                        else -> ReadableType.String
                    }
                )
            ) {

                ReadableMapFieldState.WrongType -> {
                    errorObtained(
                        keyWrongTypeMessage(
                            key = key,
                            type = when(key) {
                                enabledKey, autoClearKey -> ReadableType.Boolean
                                else -> ReadableType.String
                            }
                        )
                    )
                }

                ReadableMapFieldState.NotExists -> errorObtained(missingKeyMessage(key))

                ReadableMapFieldState.Correct -> when(key) {
                    enabledKey -> notificationsConfig.enabled = options.getBoolean(key)
                    autoClearKey -> notificationsConfig.autoClear = options.getBoolean(key)
                    notificationChannelKey -> options.getString(key)?.let {
                        notificationsConfig.notificationChannel = it
                    }
                    onProgressTitleKey ->
                        notificationsConfig.onProgressTitle = options.getString(key)
                    onErrorTitleKey ->
                        notificationsConfig.onErrorTitle = options.getString(key)
                    onErrorMessageKey ->
                        notificationsConfig.onErrorMessage = options.getString(key)
                    onCancelledTitleKey ->
                        notificationsConfig.onCancelledTitle = options.getString(key)
                    onCancelledMessageKey ->
                        notificationsConfig.onCancelledMessage = options.getString(key)
                }

            }
        }

        optionsObtained(notificationsConfig)
    }
}