package com.vydia.RNUploader.networking.request.options

import com.facebook.react.bridge.ReadableMap
import com.vydia.RNUploader.networking.request.RequestType
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`

class RequestOptionsProviderImplTest {

    @Mock
    private lateinit var mockOptions: ReadableMap

    @Mock
    private lateinit var mockRequestOptions: RequestOptions

    private lateinit var requestOptionsProvider: RequestOptionsProviderImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        requestOptionsProvider = RequestOptionsProviderImpl()
    }

    @Test
    fun testObtainRequestOptions_withRawType() {
        `when`(mockOptions.hasKey(typeKey)).thenReturn(true)
        `when`(mockOptions.getString(typeKey)).thenReturn("raw")

        requestOptionsProvider.obtainRequestOptions(
            mockOptions,
            { requestOptions ->
                assert(requestOptions.type == RequestType.Raw)
            },
            { error ->  }
        )
    }

    @Test
    fun testObtainRequestOptions_withMultipartType() {
        `when`(mockOptions.hasKey(typeKey)).thenReturn(true)
        `when`(mockOptions.getString(typeKey)).thenReturn("multipart")

        requestOptionsProvider.obtainRequestOptions(
            mockOptions,
            { requestOptions ->
                assert(requestOptions.type == RequestType.Multipart)
            },
            { error ->  }
        )
    }

    @Test
    fun testObtainRequestOptions_withInvalidType() {
        `when`(mockOptions.hasKey(typeKey)).thenReturn(true)
        `when`(mockOptions.getString(typeKey)).thenReturn("invalid")

        requestOptionsProvider.obtainRequestOptions(
            mockOptions,
            { requestOptions ->  },
            { error ->
                assert(error == "Wrong request type")
            }
        )
    }
}