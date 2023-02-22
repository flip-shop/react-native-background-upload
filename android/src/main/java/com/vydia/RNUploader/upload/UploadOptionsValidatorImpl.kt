package com.vydia.RNUploader.upload

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.vydia.RNUploader.ReadableMapFieldState
import com.vydia.RNUploader.files.*
import com.vydia.RNUploader.obtainFieldState

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
                onMissingArgument(keyWrongTypeMessage(key, ReadableType.String))
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
        onValidationSuccess()
    }

    override fun obtainUploadOptions(
        options: ReadableMap,
        uploadOptionsObtained: (UploadOptions) -> Unit,
        uploadOptionsObtainError: (String) -> Unit
    ) {
        val uploadOptions = UploadOptions()

        when(
            obtainFieldState(
                map = options,
                fieldNameKey = urlKey,
                requiredFieldType = ReadableType.String
            )
        ) {
            ReadableMapFieldState.WrongType -> {
                uploadOptionsObtainError(keyWrongTypeMessage(urlKey, ReadableType.String))
                return
            }
            ReadableMapFieldState.NotExists -> {
                uploadOptionsObtainError(missingKeyMessage(urlKey))
                return
            }
            ReadableMapFieldState.Correct -> uploadOptions.uploadUrl = options.getString(urlKey)
        }

        when(
            obtainFieldState(
                map = options,
                fieldNameKey = pathKey,
                requiredFieldType = ReadableType.String
            )
        ) {
            ReadableMapFieldState.WrongType -> {
                uploadOptionsObtainError(keyWrongTypeMessage(pathKey, ReadableType.String))
                return
            }
            ReadableMapFieldState.NotExists -> {
                uploadOptionsObtainError(missingKeyMessage(pathKey))
                return
            }
            ReadableMapFieldState.Correct ->
                uploadOptions.fileToUploadPath = options.getString(pathKey)
        }


    }



}
