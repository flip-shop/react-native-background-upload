package com.vydia.RNUploader.networking.request.options

import com.facebook.react.bridge.ReadableMap

interface RequestOptionsProvider {
    fun obtainRequestOptions(
        options: ReadableMap,
        requestOptionsObtained: (RequestOptions) -> Unit = {},
        onErrorObtained: (String) -> Unit = {}
    )
}