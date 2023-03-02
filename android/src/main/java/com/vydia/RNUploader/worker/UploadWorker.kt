package com.vydia.RNUploader.worker

import android.content.Context
import android.util.Log.d
import androidx.work.*
import com.facebook.react.bridge.Arguments
import com.vydia.RNUploader.emptyString
import com.vydia.RNUploader.event_publisher.*
import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.files.helpers.deserializeFromJson
import com.vydia.RNUploader.files.helpers.serializeToJson
import com.vydia.RNUploader.unknownExceptionMessage
import com.vydia.RNUploader.networking.UploadRepository
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import com.vydia.RNUploader.notifications.config.NotificationsConfig
import com.vydia.RNUploader.notifications.data.BackgroundUploadNotification
import com.vydia.RNUploader.notifications.data.NotificationType
import com.vydia.RNUploader.notifications.manager.NotificationCreator
import com.vydia.RNUploader.notifications.manager.NotificationCreatorImpl
import com.vydia.RNUploader.responseOk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.Response
import org.koin.java.KoinJavaComponent.inject

private const val TAG = "UploadWorker"
class UploadWorker(
    appContext: Context,
    params: WorkerParameters
): Worker(appContext, params) {

    //
    private var uploadJob: Job? = null

    //
    private val uploadRepository: UploadRepository
        by inject(UploadRepository::class.java)

    //
    private val eventsPublisher: EventsPublisher
        by inject(EventsPublisherImpl::class.java)

    //
    private val notificationsCreator: NotificationCreator
        by inject(NotificationCreatorImpl::class.java)

    //
    private var uploadId: String? = null


    override fun doWork(): Result {

        uploadJob = Job()

        val uploadFileInfo = inputData.getString(fileInfoKey)?.let {
            deserializeFromJson<FileInfo>(it)
        }

        val requestOptions = inputData.getString(requestOptionsKey)?.let {
            deserializeFromJson<UploadRequestOptions>(it)
        }

        val httpClientOptions = inputData.getString(httpClientOptionsKey)?.let {
            deserializeFromJson<HttpClientOptions>(it)
        }

        val notificationsConfig = inputData.getString(notificationsConfigKey)?.let {
            deserializeFromJson<NotificationsConfig>(it)
        }

        if(uploadFileInfo == null || requestOptions == null ||
            httpClientOptions == null || notificationsConfig == null) {
            return Result.failure()
        }

        uploadId = requestOptions.customUploadId

        uploadJob?.let { job ->
            CoroutineScope(Dispatchers.IO + job).launch {
                uploadRepository.upload(
                    fileInfo = uploadFileInfo,
                    requestOptions = requestOptions,
                    httpClientOptions = httpClientOptions,
                    onProgress = { progress ->

                        //
                        eventsPublisher.sendEvent(
                            event = UploadEvents.UploadProgress,
                            params = Arguments.createMap().apply {
                                putString(uploadIdParamName, uploadId)
                                putInt(uploadProgressParamName, progress)
                            }
                        )

                        //
                        notificationsCreator.displayNotification(
                            notification = BackgroundUploadNotification(
                                type = NotificationType.Progress,
                                title = notificationsConfig.onProgressTitle,
                                body = null,
                                progress = progress
                            ),
                            notificationsConfig = notificationsConfig
                        )
                    },
                    onResponse = { response ->

                        //
                        eventsPublisher.sendEvent(
                            event = UploadEvents.UploadSuccess,
                            params = Arguments.createMap().apply {
                                putString(uploadIdParamName, uploadId)
                                putInt(uploadResponseCodeParamName, response.code)
                                putString(uploadResponseBodyParamName, response.body?.string())
                                putString(uploadResponseHeadersParamName, response.headers.toString())
                            }
                        )

                        //
                        notificationsCreator.removeNotification(NotificationType.Progress)

                        //
                        if(response.code != responseOk) {
                            notificationsCreator.displayNotification(
                                notification = BackgroundUploadNotification(
                                    type = NotificationType.Error,
                                    title = notificationsConfig.onErrorTitle,
                                    body = notificationsConfig.onErrorMessage
                                ),
                                notificationsConfig = notificationsConfig
                            )
                        }

                        //
                        d(TAG, "upload onResponse: ${response.body}")
                    },
                    onError = {

                        //
                        eventsPublisher.sendEvent(
                            event = UploadEvents.UploadError,
                            params = Arguments.createMap().apply {
                                putString(uploadIdParamName, uploadId)
                                putString(
                                    uploadErrorMessageParamName,
                                    it.message ?: unknownExceptionMessage
                                )
                            }
                        )

                        //
                        notificationsCreator.removeNotification(NotificationType.Progress)

                        //
                        notificationsCreator.displayNotification(
                            notification = BackgroundUploadNotification(
                                type = NotificationType.Error,
                                title = notificationsConfig.onErrorTitle,
                                body = notificationsConfig.onErrorMessage
                            ),
                            notificationsConfig = notificationsConfig
                        )

                        //
                        d(TAG, "upload onError: ${it.message}")
                    }
                )
            }
        }

        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        uploadJob?.cancel()
        uploadJob = null
    }

    companion object {

        private const val fileInfoKey = "FILE_INFO"
        private const val requestOptionsKey = "REQUEST_OPTIONS"
        private const val httpClientOptionsKey = "HTTP_CLIENT_OPTIONS"
        private const val notificationsConfigKey = "NOTIFICATIONS_CONFIG"
        private const val uniqueWorkName = "FileUploadWork-"

        fun enqueue(
            workManager: WorkManager,
            fileInfo: FileInfo,
            requestOptions: UploadRequestOptions,
            httpClientOptions: HttpClientOptions,
            notificationsConfig: NotificationsConfig
        ) {
            val inputData = Data.Builder()
                .putString(fileInfoKey, serializeToJson(fileInfo))
                .putString(requestOptionsKey, serializeToJson(requestOptions))
                .putString(httpClientOptionsKey, serializeToJson(httpClientOptions))
                .putString(notificationsConfigKey, serializeToJson(notificationsConfig))
                .build()

            val request = OneTimeWorkRequest.Builder(UploadWorker::class.java)
                .addTag(requestOptions.customUploadId)
                .setInputData(inputData)
                .build()

            workManager.enqueueUniqueWork(
                "$uniqueWorkName${requestOptions.customUploadId}",
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }

}