package com.vydia.RNUploader.networking.httpClient

import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class HttpClientProviderImplTest {

    @Mock
    lateinit var options: HttpClientOptions

    lateinit var httpClientProviderImpl: HttpClientProviderImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(options.followRedirects).thenReturn(true)
        `when`(options.followSslRedirects).thenReturn(true)
        `when`(options.retryOnConnectionFailure).thenReturn(true)
        `when`(options.connectTimeout).thenReturn(30)
        `when`(options.writeTimeout).thenReturn(30)
        `when`(options.readTimeout).thenReturn(30)
        httpClientProviderImpl = HttpClientProviderImpl()
    }

    @Test
    fun provide_returnsCorrectOkHttpClient() {
        val httpClient: OkHttpClient = httpClientProviderImpl.provide(options)
        assertEquals(true, httpClient.followRedirects)
        assertEquals(true, httpClient.followSslRedirects)
        assertEquals(true, httpClient.retryOnConnectionFailure)
        assertEquals(30, httpClient.connectTimeoutMillis / 1000)
        assertEquals(30, httpClient.writeTimeoutMillis / 1000)
        assertEquals(30, httpClient.readTimeoutMillis / 1000)
    }
}
