package com.vydia.RNUploader.networking

interface HttpClientOptionsProvider {
    fun obtainHttpClientOptions(
        options: ReadableMap,
        httpOptionsObtained: (HttpClientOptions) -> Unit = {},
        wrongOptionType: (String) -> Unit = {}
    )
}