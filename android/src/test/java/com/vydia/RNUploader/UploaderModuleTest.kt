package com.vydia.RNUploader

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.files.FileInfoProvider
import com.vydia.RNUploader.helpers.uploadRequestOptionsNullExceptionsMessage
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.httpClient.HttpClientOptionsProvider
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import com.vydia.RNUploader.networking.request.options.UploadRequestOptionsProvider
import com.vydia.RNUploader.notifications.config.NotificationsConfig
import com.vydia.RNUploader.notifications.config.NotificationsConfigProvider
import com.vydia.RNUploader.notifications.manager.NotificationChannelManager
import com.vydia.RNUploader.worker.UploadWorkerInitializer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class UploaderModuleTest {

    @Mock
    private lateinit var mockReactContext: ReactApplicationContext

    @Mock
    private lateinit var mockFileInfoProvider: FileInfoProvider

    @Mock
    private lateinit var mockHttpClientOptionsProvider: HttpClientOptionsProvider

    @Mock
    private lateinit var mockUploadRequestOptionsProvider: UploadRequestOptionsProvider

    @Mock
    private lateinit var mockNotificationsConfigProvider: NotificationsConfigProvider

    @Mock
    private lateinit var mockNotificationChannelManager: NotificationChannelManager

    @Mock
    private lateinit var mockWorkerInitializer: UploadWorkerInitializer

    @Mock
    private lateinit var mockOptions: ReadableMap

    @Mock
    private lateinit var mockPromise: Promise

    private lateinit var uploaderModule: UploaderModule

    private val fileInfo = FileInfo(
        name ="testFile.mp4",
        extension = "mp4",
        mimeType = "video/mp4",
        size ="1000",
        exists = true
    )

    private val uploadRequestOptions = UploadRequestOptions(customUploadId = "customUploadId")
    private val httpClientOptions = HttpClientOptions()
    private val notificationsConfig = NotificationsConfig()

    private val errorMessage = "Error Message"
    private val errorException = IllegalArgumentException(errorMessage)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        uploaderModule = UploaderModule(
            mockReactContext,
            mockFileInfoProvider,
            mockHttpClientOptionsProvider,
            mockUploadRequestOptionsProvider,
            mockNotificationsConfigProvider,
            mockNotificationChannelManager,
            mockWorkerInitializer
        )
    }

    @Test
    fun `test getFileInfo method with valid path`() {

        `when`(mockFileInfoProvider.getFileInfoFromPath(anyString(), any(), any())).thenAnswer {
            val onFileInfoObtained: (FileInfo) -> Unit = it.getArgument(1)
            onFileInfoObtained(fileInfo)
        }

        // Act
        uploaderModule.getFileInfo(
            path = "/storage/extSdCard/DCIM/Camera/20161116_074726.mp4",
            mockPromise
        )

        // Assert
        verify(mockPromise).resolve(fileInfo.toArgumentsMap())
    }

    @Test
    fun `test startUpload method with valid all options`() {

        `when`(mockUploadRequestOptionsProvider.obtainUploadOptions(any(), any(), any())
        ).thenAnswer {
            val uploadOptionsObtained: (UploadRequestOptions) -> Unit = it.getArgument(1)
            uploadOptionsObtained(uploadRequestOptions)
        }


        `when`(mockHttpClientOptionsProvider.obtainHttpClientOptions(any(), any(), any())).thenAnswer {
            val httpOptionsObtained: (HttpClientOptions) -> Unit = it.getArgument(1)
            httpOptionsObtained(httpClientOptions)
        }

        `when`(mockNotificationsConfigProvider.provide(any(), any(), any())).thenAnswer {
            val optionsObtained: (NotificationsConfig) -> Unit = it.getArgument(1)
            optionsObtained(notificationsConfig)
        }

        `when`(mockFileInfoProvider.getFileInfoFromPath(any(), any(), any())).thenAnswer {
            val onFileInfoObtained: (FileInfo) -> Unit = it.getArgument(1)
            onFileInfoObtained(fileInfo)
        }

        uploaderModule.startUpload(mockOptions, mockPromise)

        verify(mockUploadRequestOptionsProvider).obtainUploadOptions(any(), any(), any())
        verify(mockHttpClientOptionsProvider).obtainHttpClientOptions(any(), any(), any())
        verify(mockNotificationsConfigProvider).provide(any(), any(), any())
        verify(mockFileInfoProvider).getFileInfoFromPath(any(), any(), any())
        verify(mockWorkerInitializer).startWorker(
            fileInfo,
            uploadRequestOptions,
            httpClientOptions,
            notificationsConfig
        )
        verify(mockPromise).resolve("customUploadId")
    }

    @Test
    fun `test startUpload method with obtainUploadOptions error`() {

        `when`(mockUploadRequestOptionsProvider.obtainUploadOptions(any(), any(), any())
        ).thenAnswer {
            val errorObtained: (String) -> Unit = it.getArgument(2)
            errorObtained(errorMessage)
        }

        uploaderModule.startUpload(mockOptions, mockPromise)

        verify(mockUploadRequestOptionsProvider).obtainUploadOptions(any(), any(), any())
        verify(mockPromise).reject(errorException)
        verifyNoInteractions(mockHttpClientOptionsProvider)
        verifyNoInteractions(mockNotificationsConfigProvider)
        verifyNoInteractions(mockFileInfoProvider)
        verifyNoInteractions(mockWorkerInitializer)
    }

    @Test
    fun `test cancelUpload method with valid upload ID`() {
        //TODO: write test case for cancelUpload method with valid upload ID
    }

    @Test
    fun `test cancelUpload method with invalid upload ID`() {
        //TODO: write test case for cancelUpload method with invalid upload ID
    }

    @Test
    fun `test stopAllUploads method`() {
        //TODO: write test case for stopAllUploads method
    }
}