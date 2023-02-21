package com.vydia.RNUploader.networking

data class HttpClientOptions(
    var followRedirects: Boolean = true,
    var followSslRedirects: Boolean = true,
    var retryOnConnectionFailure: Boolean = true,
    var connectTimeout: Int = 15,
    var writeTimeout: Int = 15,
    var readTimeout: Int = 30
)