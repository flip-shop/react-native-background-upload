package com.vydia.RNUploader.networking.httpClient

import com.facebook.react.bridge.ReadableMap

interface HttpClientOptionsProvider {
    fun obtainHttpClientOptions(
        options: ReadableMap,
        httpOptionsObtained: (HttpClientOptions) -> Unit = {},
        wrongOptionType: (String) -> Unit = {}
    )
}