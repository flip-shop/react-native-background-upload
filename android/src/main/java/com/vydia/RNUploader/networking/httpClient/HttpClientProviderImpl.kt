package com.vydia.RNUploader.networking.httpClient

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class HttpClientProviderImpl: HttpClientProvider {
    override fun provide(options: HttpClientOptions): OkHttpClient =
        OkHttpClient.Builder()
            .followRedirects(options.followRedirects)
            .followSslRedirects(options.followSslRedirects)
            .retryOnConnectionFailure(options.retryOnConnectionFailure)
            .connectTimeout(options.connectTimeout, TimeUnit.SECONDS)
            .writeTimeout(options.writeTimeout, TimeUnit.SECONDS)
            .readTimeout(options.readTimeout, TimeUnit.SECONDS)
            .build()
}