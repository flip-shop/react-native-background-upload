package com.vydia.RNUploader.networking.httpClient

import okhttp3.OkHttpClient

interface HttpClientProvider {
    fun provide(options: HttpClientOptions): OkHttpClient
}