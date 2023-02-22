package com.vydia.RNUploader.upload

import com.facebook.react.bridge.ReadableMap

interface UploadOptionsValidator {
    fun validate(
        uploadOptions: ReadableMap,
        onMissingArgument: (String) -> Unit = {},
        onValidationSuccess: () -> Unit = {}
    )

    fun obtainUploadOptions(
        options: ReadableMap,
        uploadOptionsObtained: (UploadOptions) -> Unit = {},
        uploadOptionsObtainError: (String) -> Unit = {}
    )
}