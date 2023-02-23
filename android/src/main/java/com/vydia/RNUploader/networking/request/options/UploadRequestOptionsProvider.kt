package com.vydia.RNUploader.networking.request.options

import com.facebook.react.bridge.ReadableMap

interface UploadRequestOptionsProvider {

    fun obtainUploadOptions(
        options: ReadableMap,
        uploadOptionsObtained: (UploadRequestOptions) -> Unit = {},
        uploadOptionsObtainError: (String) -> Unit = {}
    )
}