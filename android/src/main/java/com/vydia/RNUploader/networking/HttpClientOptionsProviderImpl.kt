package com.vydia.RNUploader.networking

import com.vydia.RNUploader.files.*

class HttpClientOptionsProviderImpl: HttpClientOptionsProvider {
    override fun obtainHttpClientOptions(
        options: ReadableMap,
        httpOptionsObtained: (HttpClientOptions) -> Unit,
        wrongOptionType: (String) -> Unit
    ) {

        val httpClientOptions = HttpClientOptions()

        //
        if (options.hasKey(followRedirectsKey)) {
            if (options.getType(followRedirectsKey) != ReadableType.Boolean) {
                wrongOptionType(followRedirectsWrongTypeMessage)
                return
            }
            httpClientOptions.followRedirects = options.getBoolean(followRedirectsKey)
        }

        //
        if (options.hasKey(followSslRedirectsKey)) {
            if (options.getType(followSslRedirectsKey) != ReadableType.Boolean) {
                wrongOptionType(followSSLRedirectsWrongTypeMessage)
                return
            }
            httpClientOptions.followSslRedirects = options.getBoolean(followSslRedirectsKey)
        }

        //
        if (options.hasKey(retryOnConnectionFailureKey)) {
            if (options.getType(retryOnConnectionFailureKey) != ReadableType.Boolean) {
                wrongOptionType(retryOnConnectionFailureWrongTypeMessage)
                return
            }
            httpClientOptions.retryOnConnectionFailure = options.getBoolean(retryOnConnectionFailureKey)
        }

        //
        if (options.hasKey(connectTimeoutKey)) {
            if (options.getType(connectTimeoutKey) != ReadableType.Number) {
                wrongOptionType(connectTimeoutWrongTypeMessage)
                return
            }
            httpClientOptions.connectTimeout = options.getInt(connectTimeoutKey)
        }

        //
        if (options.hasKey(writeTimeoutKey)) {
            if (options.getType(writeTimeoutKey) != ReadableType.Number) {
                wrongOptionType(writeTimeoutWrongTypeMessage)
                return
            }
            httpClientOptions.writeTimeout = options.getInt(writeTimeoutKey)
        }

        //
        if (options.hasKey(readTimeoutKey)) {
            if (options.getType(readTimeoutKey) != ReadableType.Number) {
                wrongOptionType(readTimeoutWrongTypeMessage)
                return
            }
            httpClientOptions.readTimeout = options.getInt(readTimeoutKey)
        }

        //
        httpOptionsObtained(httpClientOptions)
    }
}