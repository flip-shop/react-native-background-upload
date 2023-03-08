package com.vydia.RNUploader.networking.httpClient

import com.vydia.RNUploader.helpers.defaultConnectTimeout
import com.vydia.RNUploader.helpers.defaultReadTimeout
import com.vydia.RNUploader.helpers.defaultWriteTimeout

data class HttpClientOptions(
    var followRedirects: Boolean = true,
    var followSslRedirects: Boolean = true,
    var retryOnConnectionFailure: Boolean = true,
    var connectTimeout: Long = defaultConnectTimeout,
    var writeTimeout: Long = defaultWriteTimeout,
    var readTimeout: Long = defaultReadTimeout
)