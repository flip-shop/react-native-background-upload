package com.vydia.RNUploader.upload

interface UploadOptionsValidator {
    fun validate(
        uploadOptions: ReadableMap,
        onMissingArgument: (String) -> Unit = {},
        onValidationSuccess: () -> Unit = {}
    )
}