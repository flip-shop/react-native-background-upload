package com.vydia.RNUploader.notifications

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.vydia.RNUploader.*
import com.vydia.RNUploader.helpers.keyWrongTypeMessage
import com.vydia.RNUploader.helpers.missingKeyMessage
import com.vydia.RNUploader.notifications.config.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

class NotificationsConfigProviderImplTest {

    private lateinit var configProvider: NotificationsConfigProvider

    @Mock
    private lateinit var mockOptions: ReadableMap

    @Mock
    private lateinit var mockNotificationObject: ReadableMap

    @Mock
    private lateinit var errorObtained: (String) -> Unit

    @Mock
    private lateinit var configObtained: (NotificationsConfig) -> Unit

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        configProvider = NotificationsConfigProviderImpl()
    }

    @Test
    fun testProvideSuccess() {

        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(enabledKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(enabledKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(onProgressTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onProgressTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onProgressTitleKey)).thenReturn(mockOnProgressTitle)

        `when`(mockNotificationObject.hasKey(onErrorTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onErrorTitleKey)).thenReturn(mockOnErrorTitle)

        `when`(mockNotificationObject.hasKey(onErrorMessageKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorMessageKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onErrorMessageKey)).thenReturn(mockOnErrorMessage)

        `when`(mockNotificationObject.hasKey(onCancelledTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onCancelledTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onCancelledTitleKey)).thenReturn(mockOnCancelledTitle)

        `when`(mockNotificationObject.hasKey(onCancelledMessageKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onCancelledMessageKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onCancelledMessageKey)).thenReturn(mockOnCancelledMessage)


        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        val successArgumentCaptor = argumentCaptor<NotificationsConfig>()
        verify(configObtained).invoke(successArgumentCaptor.capture())
        val obtainedOptions = successArgumentCaptor.firstValue

        //
        assertNotNull(obtainedOptions)
        Mockito.verifyNoInteractions(errorObtained)
        assertEquals(true, obtainedOptions.autoClear)
        assertEquals(true, obtainedOptions.enabled)
        assertEquals(mockOnProgressTitle, obtainedOptions.onProgressTitle)
        assertEquals(mockOnErrorTitle, obtainedOptions.onErrorTitle)
        assertEquals(mockOnErrorMessage, obtainedOptions.onErrorMessage)
        assertEquals(mockOnCancelledTitle, obtainedOptions.onCancelledTitle)
    }

    @Test
    fun notificationNullTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(null)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        val successArgumentCaptor = argumentCaptor<NotificationsConfig>()
        verify(configObtained).invoke(successArgumentCaptor.capture())
        val obtainedOptions = successArgumentCaptor.firstValue

        //
        assertNotNull(obtainedOptions)
        Mockito.verifyNoInteractions(errorObtained)
        assertEquals(false, obtainedOptions.enabled)
    }

    @Test
    fun missingAutoClearValueTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)
        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(false)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(missingKeyMessage(autoClearKey))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun autoClearValueWrongTypeTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.String)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(keyWrongTypeMessage(autoClearKey, ReadableType.Boolean))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun missingEnabledValueTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(false)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(missingKeyMessage(enabledKey))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun enabledValueWrongTypeTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(enabledKey)).thenReturn(ReadableType.String)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(keyWrongTypeMessage(enabledKey, ReadableType.Boolean))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun missingOnProgressTitleValueTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(enabledKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(enabledKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(onProgressTitleKey)).thenReturn(false)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(missingKeyMessage(onProgressTitleKey))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun onProgressTitleValueWrongTypeTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(enabledKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(enabledKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(onProgressTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onProgressTitleKey)).thenReturn(ReadableType.Boolean)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(keyWrongTypeMessage(onProgressTitleKey, ReadableType.String))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun missingOnErrorTitleValueTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(enabledKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(enabledKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(onProgressTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onProgressTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onProgressTitleKey)).thenReturn(mockOnProgressTitle)

        `when`(mockNotificationObject.hasKey(onErrorTitleKey)).thenReturn(false)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(missingKeyMessage(onErrorTitleKey))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun onErrorTitleValueWrongTypeTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(enabledKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(enabledKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(onProgressTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onProgressTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onProgressTitleKey)).thenReturn(mockOnProgressTitle)

        `when`(mockNotificationObject.hasKey(onErrorTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorTitleKey)).thenReturn(ReadableType.Boolean)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(keyWrongTypeMessage(onErrorTitleKey, ReadableType.String))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun missingOnErrorMessageValueTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(enabledKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(enabledKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(onProgressTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onProgressTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onProgressTitleKey)).thenReturn(mockOnProgressTitle)

        `when`(mockNotificationObject.hasKey(onErrorTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onErrorTitleKey)).thenReturn(mockOnErrorTitle)

        `when`(mockNotificationObject.hasKey(onErrorMessageKey)).thenReturn(false)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(missingKeyMessage(onErrorMessageKey))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun onErrorMessageValueWrongTypeTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(enabledKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(enabledKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(onProgressTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onProgressTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onProgressTitleKey)).thenReturn(mockOnProgressTitle)

        `when`(mockNotificationObject.hasKey(onErrorTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onErrorTitleKey)).thenReturn(mockOnErrorTitle)

        `when`(mockNotificationObject.hasKey(onErrorMessageKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorMessageKey)).thenReturn(ReadableType.Boolean)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(keyWrongTypeMessage(onErrorMessageKey, ReadableType.String))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun missingOnCancelledTitleKeyValueTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(enabledKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(enabledKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(onProgressTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onProgressTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onProgressTitleKey)).thenReturn(mockOnProgressTitle)

        `when`(mockNotificationObject.hasKey(onErrorTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onErrorTitleKey)).thenReturn(mockOnErrorTitle)

        `when`(mockNotificationObject.hasKey(onErrorMessageKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorMessageKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onErrorMessageKey)).thenReturn(mockOnErrorMessage)

        `when`(mockNotificationObject.hasKey(onCancelledTitleKey)).thenReturn(false)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(missingKeyMessage(onCancelledTitleKey))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun onCancelledTitleKeyValueWrongTypeTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(enabledKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(enabledKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(onProgressTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onProgressTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onProgressTitleKey)).thenReturn(mockOnProgressTitle)

        `when`(mockNotificationObject.hasKey(onErrorTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onErrorTitleKey)).thenReturn(mockOnErrorTitle)

        `when`(mockNotificationObject.hasKey(onErrorMessageKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorMessageKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onErrorMessageKey)).thenReturn(mockOnErrorMessage)

        `when`(mockNotificationObject.hasKey(onCancelledTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onCancelledTitleKey)).thenReturn(ReadableType.Boolean)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(keyWrongTypeMessage(onCancelledTitleKey, ReadableType.String))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun missingOnCancelledMessageKeyValueTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(enabledKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(enabledKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(onProgressTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onProgressTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onProgressTitleKey)).thenReturn(mockOnProgressTitle)

        `when`(mockNotificationObject.hasKey(onErrorTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onErrorTitleKey)).thenReturn(mockOnErrorTitle)

        `when`(mockNotificationObject.hasKey(onErrorMessageKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorMessageKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onErrorMessageKey)).thenReturn(mockOnErrorMessage)

        `when`(mockNotificationObject.hasKey(onCancelledTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onCancelledTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onCancelledTitleKey)).thenReturn(mockOnCancelledTitle)

        `when`(mockNotificationObject.hasKey(onCancelledMessageKey)).thenReturn(false)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(missingKeyMessage(onCancelledMessageKey))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }

    @Test
    fun onCancelledMessageKeyValueWrongTypeTest() {
        `when`(mockOptions.getMap(notificationsMapKey)).thenReturn(mockNotificationObject)

        `when`(mockNotificationObject.hasKey(autoClearKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(autoClearKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(autoClearKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(enabledKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(enabledKey)).thenReturn(ReadableType.Boolean)
        `when`(mockNotificationObject.getBoolean(enabledKey)).thenReturn(true)

        `when`(mockNotificationObject.hasKey(onProgressTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onProgressTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onProgressTitleKey)).thenReturn(mockOnProgressTitle)

        `when`(mockNotificationObject.hasKey(onErrorTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onErrorTitleKey)).thenReturn(mockOnErrorTitle)

        `when`(mockNotificationObject.hasKey(onErrorMessageKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onErrorMessageKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onErrorMessageKey)).thenReturn(mockOnErrorMessage)

        `when`(mockNotificationObject.hasKey(onCancelledTitleKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onCancelledTitleKey)).thenReturn(ReadableType.String)
        `when`(mockNotificationObject.getString(onCancelledTitleKey)).thenReturn(mockOnCancelledTitle)

        `when`(mockNotificationObject.hasKey(onCancelledMessageKey)).thenReturn(true)
        `when`(mockNotificationObject.getType(onCancelledMessageKey)).thenReturn(ReadableType.Boolean)

        //
        configProvider.provide(
            options = mockOptions,
            optionsObtained = configObtained,
            errorObtained = errorObtained
        )

        verify(errorObtained).invoke(keyWrongTypeMessage(onCancelledMessageKey, ReadableType.String))
        verifyNoMoreInteractions(configObtained, errorObtained)
    }


}