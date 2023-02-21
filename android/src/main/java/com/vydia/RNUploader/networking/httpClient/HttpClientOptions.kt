package com.vydia.RNUploader.networking.httpClient

import com.vydia.RNUploader.defaultConnectTimeout
import com.vydia.RNUploader.defaultReadTimeout
import com.vydia.RNUploader.defaultWriteTimeout

data class HttpClientOptions(
    var followRedirects: Boolean = true,
    var followSslRedirects: Boolean = true,
    var retryOnConnectionFailure: Boolean = true,
    var connectTimeout: Int = defaultConnectTimeout,
    var writeTimeout: Int = defaultWriteTimeout,
    var readTimeout: Int = defaultReadTimeout
)