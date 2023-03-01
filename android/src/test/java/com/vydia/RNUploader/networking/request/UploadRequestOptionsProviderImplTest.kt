package com.vydia.RNUploader.networking.request

import com.facebook.react.bridge.JavaOnlyMap
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.vydia.RNUploader.*
import com.vydia.RNUploader.keyWrongTypeMessage
import com.vydia.RNUploader.missingKeyMessage
import com.vydia.RNUploader.networking.request.options.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor

class UploadRequestOptionsProviderImplTest {

    private lateinit var requestOptionsProvider: UploadRequestOptionsProviderImpl

    @Mock
    private lateinit var mockOptions: ReadableMap

    @Mock
    private lateinit var uploadOptionsObtainError: (String) -> Unit

    @Mock
    private lateinit var uploadOptionsObtained: (UploadRequestOptions) -> Unit

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        requestOptionsProvider = UploadRequestOptionsProviderImpl()
    }

    @Test
    fun testProvideSuccess() {

        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.getString(urlKey)).thenReturn(mockUrl)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.getString(pathKey)).thenReturn(mockFilePath)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.Map)
        `when`(mockOptions.getMap(headersKey)).thenReturn(JavaOnlyMap.from(mockHeaders))

        `when`(mockOptions.hasKey(methodKey)).thenReturn(true)
        `when`(mockOptions.getType(methodKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.getString(methodKey)).thenReturn(mockMethod)

        `when`(mockOptions.hasKey(maxRetriesKey)).thenReturn(true)
        `when`(mockOptions.getType(maxRetriesKey)).thenReturn(ReadableType.Number)
        `when`(mockOptions.getInt(maxRetriesKey)).thenReturn(mockMaxRetries)

        `when`(mockOptions.hasKey(customUploadIdKey)).thenReturn(true)
        `when`(mockOptions.getType(customUploadIdKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.getString(customUploadIdKey)).thenReturn(mockUploadId)

        `when`(mockOptions.hasKey(requestFieldNameKey)).thenReturn(true)
        `when`(mockOptions.getType(requestFieldNameKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.getString(requestFieldNameKey)).thenReturn(mockRequestFieldName)

        `when`(mockOptions.hasKey(requestTypeKey)).thenReturn(true)
        `when`(mockOptions.getType(requestTypeKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.getString(requestTypeKey)).thenReturn(mockRequestType)


        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        val successArgumentCaptor = argumentCaptor<UploadRequestOptions>()
        verify(uploadOptionsObtained).invoke(successArgumentCaptor.capture())
        val obtainedOptions = successArgumentCaptor.firstValue

        assertNotNull(obtainedOptions)
        verifyNoInteractions(uploadOptionsObtainError)

        assertEquals(mockUrl, obtainedOptions.uploadUrl)
        assertEquals(mockFilePath, obtainedOptions.fileToUploadPath)
        assertEquals(mockHeaders, obtainedOptions.headers)
        assertEquals(mockMethod, obtainedOptions.method)
        assertEquals(mockMaxRetries, obtainedOptions.maxRetries)
        assertEquals(mockUploadId, obtainedOptions.customUploadId)
        assertEquals(mockRequestFieldName, obtainedOptions.requestFieldName)
        assertEquals(multipartRequestType, obtainedOptions.requestType)
    }


    @Test
    fun testMissingUrl() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(false)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(missingKeyMessage(urlKey))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testUrlWrongType() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.Number)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        val argumentCaptor = argumentCaptor<String>()
        verify(uploadOptionsObtainError).invoke(argumentCaptor.capture())
        verify(uploadOptionsObtainError).invoke(keyWrongTypeMessage(urlKey, ReadableType.String))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }


    @Test
    fun testMissingPath() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(false)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(missingKeyMessage(pathKey))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testPathWrongType() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.Number)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(keyWrongTypeMessage(pathKey, ReadableType.String))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testMissingHeaders() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(false)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.String)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(missingKeyMessage(headersKey))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testHeadersWrongType() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.Number)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(keyWrongTypeMessage(headersKey, ReadableType.String))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }


    @Test
    fun testMissingMethod() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(methodKey)).thenReturn(false)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(missingKeyMessage(methodKey))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testMethodWrongType() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(methodKey)).thenReturn(true)
        `when`(mockOptions.getType(methodKey)).thenReturn(ReadableType.Number)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(keyWrongTypeMessage(methodKey, ReadableType.String))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testMissingMaxRetries() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(methodKey)).thenReturn(true)
        `when`(mockOptions.getType(methodKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(maxRetriesKey)).thenReturn(false)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(missingKeyMessage(maxRetriesKey))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testMaxRetriesWrongType() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(methodKey)).thenReturn(true)
        `when`(mockOptions.getType(methodKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(maxRetriesKey)).thenReturn(true)
        `when`(mockOptions.getType(maxRetriesKey)).thenReturn(ReadableType.String)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(keyWrongTypeMessage(maxRetriesKey, ReadableType.Number))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testMissingCustomUploadId() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(methodKey)).thenReturn(true)
        `when`(mockOptions.getType(methodKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(maxRetriesKey)).thenReturn(true)
        `when`(mockOptions.getType(maxRetriesKey)).thenReturn(ReadableType.Number)

        `when`(mockOptions.hasKey(customUploadIdKey)).thenReturn(false)
        `when`(mockOptions.getType(customUploadIdKey)).thenReturn(ReadableType.String)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(missingKeyMessage(customUploadIdKey))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testCustomUploadIdWrongType() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(methodKey)).thenReturn(true)
        `when`(mockOptions.getType(methodKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(maxRetriesKey)).thenReturn(true)
        `when`(mockOptions.getType(maxRetriesKey)).thenReturn(ReadableType.Number)

        `when`(mockOptions.hasKey(customUploadIdKey)).thenReturn(true)
        `when`(mockOptions.getType(customUploadIdKey)).thenReturn(ReadableType.Map)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(keyWrongTypeMessage(customUploadIdKey, ReadableType.String))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testMissingRequestFieldName() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.Map)

        `when`(mockOptions.hasKey(methodKey)).thenReturn(true)
        `when`(mockOptions.getType(methodKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(maxRetriesKey)).thenReturn(true)
        `when`(mockOptions.getType(maxRetriesKey)).thenReturn(ReadableType.Number)

        `when`(mockOptions.hasKey(customUploadIdKey)).thenReturn(true)
        `when`(mockOptions.getType(customUploadIdKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(requestFieldNameKey)).thenReturn(false)


        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(missingKeyMessage(requestFieldNameKey))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testFieldNameWrongType() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.Map)

        `when`(mockOptions.hasKey(methodKey)).thenReturn(true)
        `when`(mockOptions.getType(methodKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(maxRetriesKey)).thenReturn(true)
        `when`(mockOptions.getType(maxRetriesKey)).thenReturn(ReadableType.Number)

        `when`(mockOptions.hasKey(customUploadIdKey)).thenReturn(true)
        `when`(mockOptions.getType(customUploadIdKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(requestFieldNameKey)).thenReturn(true)
        `when`(mockOptions.getType(requestFieldNameKey)).thenReturn(ReadableType.Number)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(keyWrongTypeMessage(requestFieldNameKey, ReadableType.String))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testMissingRequestType() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(methodKey)).thenReturn(true)
        `when`(mockOptions.getType(methodKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(maxRetriesKey)).thenReturn(true)
        `when`(mockOptions.getType(maxRetriesKey)).thenReturn(ReadableType.Number)

        `when`(mockOptions.hasKey(customUploadIdKey)).thenReturn(true)
        `when`(mockOptions.getType(customUploadIdKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(requestFieldNameKey)).thenReturn(true)
        `when`(mockOptions.getType(requestFieldNameKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(requestTypeKey)).thenReturn(false)


        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(missingKeyMessage(requestTypeKey))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }

    @Test
    fun testRequestTypeWrongType() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(methodKey)).thenReturn(true)
        `when`(mockOptions.getType(methodKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(maxRetriesKey)).thenReturn(true)
        `when`(mockOptions.getType(maxRetriesKey)).thenReturn(ReadableType.Number)

        `when`(mockOptions.hasKey(customUploadIdKey)).thenReturn(true)
        `when`(mockOptions.getType(customUploadIdKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(requestFieldNameKey)).thenReturn(true)
        `when`(mockOptions.getType(requestFieldNameKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(requestTypeKey)).thenReturn(true)
        `when`(mockOptions.getType(requestTypeKey)).thenReturn(ReadableType.Number)

        requestOptionsProvider.obtainUploadOptions(
            mockOptions,
            uploadOptionsObtained,
            uploadOptionsObtainError
        )

        verify(uploadOptionsObtainError).invoke(keyWrongTypeMessage(requestTypeKey, ReadableType.String))
        verifyNoMoreInteractions(uploadOptionsObtained, uploadOptionsObtainError)
    }



}