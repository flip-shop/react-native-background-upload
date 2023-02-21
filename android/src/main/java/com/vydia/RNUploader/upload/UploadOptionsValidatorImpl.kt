package com.vydia.RNUploader.upload

import com.vydia.RNUploader.files.headersWrongTypeMessage
import com.vydia.RNUploader.files.keyNotStringMessage
import com.vydia.RNUploader.files.missingKeyMessage
import com.vydia.RNUploader.files.notificationWrongTypeMessage

class UploadOptionsValidatorImpl: UploadOptionsValidator {

    override fun validate(
        options: ReadableMap,
        onMissingArgument: (String) -> Unit,
        onValidationSuccess: () -> Unit
    ) {

        for (key in arrayOf(urlKey, pathKey)) {

            // check whether options contain url and path
            if (!options.hasKey(key)) {
                onMissingArgument(missingKeyMessage(key))
                return
            }

            // check whether url and path are string type
            if (options.getType(key) != ReadableType.String) {
                onMissingArgument(keyNotStringMessage(key))
                return
            }
        }

        //
        if (options.hasKey(headersKey) && options.getType(headersKey) != ReadableType.Map) {
            onMissingArgument(headersWrongTypeMessage)
            return
        }

        //
        if (options.hasKey(notificationKey) && options.getType(notificationKey) != ReadableType.Map) {
            onMissingArgument(notificationWrongTypeMessage)
            return
        }

        //

    }
}