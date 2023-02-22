package com.vydia.RNUploader.upload

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.vydia.RNUploader.files.headersWrongTypeMessage
import com.vydia.RNUploader.files.keyNotStringMessage
import com.vydia.RNUploader.files.missingKeyMessage
import com.vydia.RNUploader.files.notificationWrongTypeMessage
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class UploadOptionsValidatorImplTest {

    private lateinit var validator: UploadOptionsValidatorImpl

    @Mock
    private lateinit var mockOptions: ReadableMap

    @Mock
    private lateinit var onMissingArgument: (String) -> Unit

    @Mock
    private lateinit var onValidationSuccess: () -> Unit

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        validator = UploadOptionsValidatorImpl()
    }

    @Test
    fun testValidateSuccess() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.Map)
        `when`(mockOptions.hasKey(notificationKey)).thenReturn(true)
        `when`(mockOptions.getType(notificationKey)).thenReturn(ReadableType.Map)

        validator.validate(mockOptions, onMissingArgument, onValidationSuccess)
        verifyNoInteractions(onMissingArgument)
    }

    @Test
    fun testValidateMissingUrl() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(false)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        validator.validate(mockOptions, onMissingArgument, onValidationSuccess)

        verify(onMissingArgument).invoke(missingKeyMessage(urlKey))
        verifyNoMoreInteractions(onMissingArgument, onValidationSuccess)
    }

    @Test
    fun testValidateMissingPath() {

        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(false)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        validator.validate(mockOptions, onMissingArgument, onValidationSuccess)

        verify(onMissingArgument).invoke(missingKeyMessage(pathKey))
        verifyNoMoreInteractions(onMissingArgument, onValidationSuccess)
    }

    @Test
    fun testValidateUrlNotString() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.Number)

        validator.validate(mockOptions, onMissingArgument, onValidationSuccess)

        verify(onMissingArgument).invoke(keyNotStringMessage(urlKey))
        verifyNoMoreInteractions(onMissingArgument, onValidationSuccess)
    }

    @Test
    fun testValidatePathNotString() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.Number)

        validator.validate(mockOptions, onMissingArgument, onValidationSuccess)

        verify(onMissingArgument).invoke(keyNotStringMessage(pathKey))
        verifyNoMoreInteractions(onMissingArgument, onValidationSuccess)
    }

    @Test
    fun testValidateHeadersNotMap() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)

        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.Null)

        validator.validate(mockOptions, onMissingArgument, onValidationSuccess)

        verify(onMissingArgument).invoke(headersWrongTypeMessage)
        verifyNoMoreInteractions(onMissingArgument, onValidationSuccess)
    }

    @Test
    fun testValidateNotificationNotMap() {
        `when`(mockOptions.hasKey(urlKey)).thenReturn(true)
        `when`(mockOptions.getType(urlKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.hasKey(pathKey)).thenReturn(true)
        `when`(mockOptions.getType(pathKey)).thenReturn(ReadableType.String)
        `when`(mockOptions.hasKey(headersKey)).thenReturn(true)
        `when`(mockOptions.getType(headersKey)).thenReturn(ReadableType.Map)
        `when`(mockOptions.hasKey(notificationKey)).thenReturn(true)
        `when`(mockOptions.getType(notificationKey)).thenReturn(ReadableType.Number)

        validator.validate(mockOptions, onMissingArgument, onValidationSuccess)

        verify(onMissingArgument).invoke(notificationWrongTypeMessage)
        verifyNoMoreInteractions(onMissingArgument, onValidationSuccess)
    }
}