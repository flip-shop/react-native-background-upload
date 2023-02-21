package com.vydia.RNUploader.upload

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.vydia.RNUploader.files.headersWrongTypeMessage
import com.vydia.RNUploader.files.keyNotStringMessage
import com.vydia.RNUploader.files.missingKeyMessage
import com.vydia.RNUploader.files.notificationWrongTypeMessage

class UploadOptionsValidatorImpl: UploadOptionsValidator {

    override fun validate(
        uploadOptions: ReadableMap,
        onMissingArgument: (String) -> Unit,
        onValidationSuccess: () -> Unit
    ) {

        for (key in arrayOf(urlKey, pathKey)) {

            // check whether options contain url and path
            if (!uploadOptions.hasKey(key)) {
                onMissingArgument(missingKeyMessage(key))
                return
            }

            // check whether url and path are string type
            if (uploadOptions.getType(key) != ReadableType.String) {
                onMissingArgument(keyNotStringMessage(key))
                return
            }
        }

        //
        if (uploadOptions.hasKey(headersKey) && uploadOptions.getType(headersKey) != ReadableType.Map) {
            onMissingArgument(headersWrongTypeMessage)
            return
        }

        //
        if (uploadOptions.hasKey(notificationKey) && uploadOptions.getType(notificationKey) != ReadableType.Map) {
            onMissingArgument(notificationWrongTypeMessage)
            return
        }

        //

    }
}