package com.vydia.RNUploader.networking.httpClient

import com.vydia.RNUploader.defaultConnectTimeout
import com.vydia.RNUploader.defaultReadTimeout
import com.vydia.RNUploader.defaultWriteTimeout

data class HttpClientOptions(
    var followRedirects: Boolean = true,
    var followSslRedirects: Boolean = true,
    var retryOnConnectionFailure: Boolean = true,
    var connectTimeout: Long = defaultConnectTimeout,
    var writeTimeout: Long = defaultWriteTimeout,
    var readTimeout: Long = defaultReadTimeout
)