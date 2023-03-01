package com.vydia.RNUploader.networking.httpClient

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.vydia.RNUploader.*
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class HttpClientOptionsProviderImplTest {

    @Mock
    private lateinit var mockOptions: ReadableMap

    private lateinit var httpClientOptionsProvider: HttpClientOptionsProviderImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        httpClientOptionsProvider = HttpClientOptionsProviderImpl()
    }



    @Test
    fun testObtainHttpClientOptions_withFollowRedirects() {
        `when`(mockOptions.hasKey(followRedirectsKey)).thenReturn(true)
        `when`(mockOptions.getType(followRedirectsKey)).thenReturn(ReadableType.Boolean)
        `when`(mockOptions.getBoolean(followRedirectsKey)).thenReturn(true)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->
                assertTrue(httpClientOptions.followRedirects)
            },
            { error -> }
        )
    }

    @Test
    fun testObtainHttpClientOptions_followRedirectsDefaultValue() {
        `when`(mockOptions.hasKey(followRedirectsKey)).thenReturn(false)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->
                assertTrue(httpClientOptions.followRedirects)
            },
            { error -> }
        )
    }

    @Test
    fun testObtainHttpClientOptions_FollowRedirectsWrongType() {
        `when`(mockOptions.hasKey(followRedirectsKey)).thenReturn(true)
        `when`(mockOptions.getType(followRedirectsKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.getBoolean(followRedirectsKey)).thenReturn(true)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->

            },
            { error ->
                assert(error == followRedirectsWrongTypeMessage)
            }
        )
    }

    @Test
    fun testObtainHttpClientOptions_withFollowSslRedirects() {
        `when`(mockOptions.hasKey(followSslRedirectsKey)).thenReturn(true)
        `when`(mockOptions.getType(followSslRedirectsKey)).thenReturn(ReadableType.Boolean)
        `when`(mockOptions.getBoolean(followSslRedirectsKey)).thenReturn(true)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->
                assertTrue(httpClientOptions.followSslRedirects)
            },
            { error -> }
        )
    }

    @Test
    fun testObtainHttpClientOptions_followSslRedirectsDefaultValue() {
        `when`(mockOptions.hasKey(followSslRedirectsKey)).thenReturn(false)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->
                assertTrue(httpClientOptions.followSslRedirects)
            },
            { error -> }
        )
    }

    @Test
    fun testObtainHttpClientOptions_FollowSslRedirectsWrongType() {
        `when`(mockOptions.hasKey(followSslRedirectsKey)).thenReturn(true)
        `when`(mockOptions.getType(followSslRedirectsKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.getBoolean(followSslRedirectsKey)).thenReturn(true)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions -> },
            { error ->
                assert(error == followSSLRedirectsWrongTypeMessage)
            }
        )
    }

    @Test
    fun testObtainHttpClientOptions_withRetryOnConnectionFailure() {
        `when`(mockOptions.hasKey(retryOnConnectionFailureKey)).thenReturn(true)
        `when`(mockOptions.getType(retryOnConnectionFailureKey)).thenReturn(ReadableType.Boolean)
        `when`(mockOptions.getBoolean(retryOnConnectionFailureKey)).thenReturn(true)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->
                assertTrue(httpClientOptions.retryOnConnectionFailure)
            },
            { error -> }
        )
    }

    @Test
    fun testObtainHttpClientOptions_retryOnConnectionFailureDefaultValue() {
        `when`(mockOptions.hasKey(retryOnConnectionFailureKey)).thenReturn(false)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->
                assertTrue(httpClientOptions.retryOnConnectionFailure)
            },
            { error -> }
        )
    }

    @Test
    fun testObtainHttpClientOptions_withRetryOnConnectionFailureWrongType() {
        `when`(mockOptions.hasKey(retryOnConnectionFailureKey)).thenReturn(true)
        `when`(mockOptions.getType(retryOnConnectionFailureKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.getBoolean(retryOnConnectionFailureKey)).thenReturn(true)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions -> },
            { error ->
                assert(error == retryOnConnectionFailureWrongTypeMessage)
            }
        )
    }

    @Test
    fun testObtainHttpClientOptions_withConnectTimeout() {
        `when`(mockOptions.hasKey(connectTimeoutKey)).thenReturn(true)
        `when`(mockOptions.getType(connectTimeoutKey)).thenReturn(ReadableType.Number)
        `when`(mockOptions.getInt(connectTimeoutKey)).thenReturn(1000)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->
                assertEquals(1000, httpClientOptions.connectTimeout)
            },
            { error -> }
        )
    }

    @Test
    fun testObtainHttpClientOptions_connectTimeoutDefaultValue() {
        `when`(mockOptions.hasKey(connectTimeoutKey)).thenReturn(false)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->
                assertEquals(defaultConnectTimeout, httpClientOptions.connectTimeout)
            },
            { error -> }
        )
    }

    @Test
    fun testObtainHttpClientOptions_withConnectTimeoutWrongType() {
        `when`(mockOptions.hasKey(connectTimeoutKey)).thenReturn(true)
        `when`(mockOptions.getType(connectTimeoutKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.getInt(connectTimeoutKey)).thenReturn(1000)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions -> },
            { error -> assert(error == connectTimeoutWrongTypeMessage) }
        )
    }

    @Test
    fun testObtainHttpClientOptions_withWriteTimeout() {
        `when`(mockOptions.hasKey(writeTimeoutKey)).thenReturn(true)
        `when`(mockOptions.getType(writeTimeoutKey)).thenReturn(ReadableType.Number)
        `when`(mockOptions.getInt(writeTimeoutKey)).thenReturn(2000)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->
                assertEquals(2000, httpClientOptions.writeTimeout)
            },
            { error -> }
        )
    }

    @Test
    fun testObtainHttpClientOptions_writeTimeoutDefaultValue() {
        `when`(mockOptions.hasKey(writeTimeoutKey)).thenReturn(false)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->
                assertEquals(defaultWriteTimeout, httpClientOptions.writeTimeout)
            },
            { error -> }
        )
    }

    @Test
    fun testObtainHttpClientOptions_withWriteTimeoutWrongType() {
        `when`(mockOptions.hasKey(writeTimeoutKey)).thenReturn(true)
        `when`(mockOptions.getType(writeTimeoutKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.getInt(writeTimeoutKey)).thenReturn(2000)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions -> },
            { error -> assert(error == writeTimeoutWrongTypeMessage) }
        )
    }

    @Test
    fun testObtainHttpClientOptions_withReadTimeout() {
        `when`(mockOptions.hasKey(readTimeoutKey)).thenReturn(true)
        `when`(mockOptions.getType(readTimeoutKey)).thenReturn(ReadableType.Number)
        `when`(mockOptions.getInt(readTimeoutKey)).thenReturn(3000)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->
                assertEquals(3000, httpClientOptions.readTimeout)
            },
            { error -> }
        )
    }

    @Test
    fun testObtainHttpClientOptions_readTimeoutDefaultValue() {
        `when`(mockOptions.hasKey(readTimeoutKey)).thenReturn(false)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions ->
                assertEquals(defaultReadTimeout, httpClientOptions.readTimeout)
            },
            { error -> }
        )
    }

    @Test
    fun testObtainHttpClientOptions_withReadTimeoutWrongType() {
        `when`(mockOptions.hasKey(readTimeoutKey)).thenReturn(true)
        `when`(mockOptions.getType(readTimeoutKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.getInt(readTimeoutKey)).thenReturn(3000)

        httpClientOptionsProvider.obtainHttpClientOptions(
            mockOptions,
            { httpClientOptions -> },
            { error -> assert(error == readTimeoutWrongTypeMessage) }
        )
    }
}
