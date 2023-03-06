package com.vydia.RNUploader.notifications.config

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.vydia.RNUploader.helpers.ReadableMapFieldState
import com.vydia.RNUploader.helpers.keyWrongTypeMessage
import com.vydia.RNUploader.helpers.missingKeyMessage
import com.vydia.RNUploader.helpers.obtainFieldState

class NotificationsConfigProviderImpl: NotificationsConfigProvider {

    override fun provide(
        options: ReadableMap,
        optionsObtained: (NotificationsConfig) -> Unit,
        errorObtained: (String) -> Unit
    ) {

        val notificationsConfig = NotificationsConfig()
        val notificationsOptionsMap = options.getMap(notificationsMapKey)

        //
        if(notificationsOptionsMap == null) {
            notificationsConfig.enabled = false
            optionsObtained(notificationsConfig)
            return
        }



        for(key in notificationConfigKeySet) {
            when(
                obtainFieldState(
                    map = notificationsOptionsMap,
                    fieldNameKey = key,
                    requiredFieldType = when(key) {
                        autoClearKey, enabledKey -> ReadableType.Boolean
                        else -> ReadableType.String
                    }
                )
            ) {

                ReadableMapFieldState.WrongType -> {
                    errorObtained(
                        keyWrongTypeMessage(
                            key = key,
                            type = when(key) {
                                autoClearKey, enabledKey -> ReadableType.Boolean
                                else -> ReadableType.String
                            }
                        )
                    )
                    return
                }

                ReadableMapFieldState.NotExists -> {
                    errorObtained(missingKeyMessage(key))
                    return
                }

                ReadableMapFieldState.Correct -> when(key) {
                    autoClearKey ->
                        notificationsConfig.autoClear = notificationsOptionsMap.getBoolean(key)
                    enabledKey ->
                        notificationsConfig.enabled = notificationsOptionsMap.getBoolean(key)
                    onProgressTitleKey ->
                        notificationsConfig.onProgressTitle = notificationsOptionsMap.getString(key)
                    onErrorTitleKey ->
                        notificationsConfig.onErrorTitle = notificationsOptionsMap.getString(key)
                    onErrorMessageKey ->
                        notificationsConfig.onErrorMessage = notificationsOptionsMap.getString(key)
                    onCancelledTitleKey ->
                        notificationsConfig.onCancelledTitle = notificationsOptionsMap.getString(key)
                    onCancelledMessageKey ->
                        notificationsConfig.onCancelledMessage = notificationsOptionsMap.getString(key)
                }

            }
        }

        optionsObtained(notificationsConfig)
    }
}