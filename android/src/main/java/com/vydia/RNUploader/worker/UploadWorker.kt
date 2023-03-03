package com.vydia.RNUploader.worker

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.*
import com.facebook.react.bridge.Arguments
import com.google.common.util.concurrent.ListenableFuture
import com.vydia.RNUploader.event_publisher.*
import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.files.helpers.deserializeFromJson
import com.vydia.RNUploader.files.helpers.serializeToJson
import com.vydia.RNUploader.helpers.responseOk
import com.vydia.RNUploader.helpers.unknownExceptionMessage
import com.vydia.RNUploader.helpers.uploadCanceled
import com.vydia.RNUploader.networking.UploadRepository
import com.vydia.RNUploader.networking.UploadRepositoryImpl
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import com.vydia.RNUploader.notifications.config.NotificationsConfig
import com.vydia.RNUploader.notifications.data.NotificationType
import com.vydia.RNUploader.notifications.manager.NotificationCreator
import com.vydia.RNUploader.notifications.manager.NotificationCreatorImpl
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
): ListenableWorker(appContext, params) {

    //
    private var uploadJob: Job? = null

    //
    private var uploadId: String? = null

    //
    private val uploadRepository: UploadRepository
        by inject(UploadRepositoryImpl::class.java)

    //
    private val eventsPublisher: EventsPublisher
        by inject(EventsPublisherImpl::class.java)

    //
    private val notificationsCreator: NotificationCreator
        by inject(NotificationCreatorImpl::class.java)

    override fun startWork(): ListenableFuture<Result> {

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

        return if(uploadFileInfo == null || requestOptions == null ||
            httpClientOptions == null || notificationsConfig == null) {
            CallbackToFutureAdapter.getFuture { completer -> completer.set(Result.failure()) }
        }else {
            CallbackToFutureAdapter.getFuture { completer ->

                uploadId = requestOptions.customUploadId
                uploadJob = Job()

                uploadJob?.let { job ->
                    CoroutineScope(Dispatchers.IO + job).launch {
                        uploadRepository.upload(
                            fileInfo = uploadFileInfo,
                            requestOptions = requestOptions,
                            httpClientOptions = httpClientOptions,
                            onProgress = {
                                handleProgressUpdate(
                                    progress = it,
                                    notificationsConfig = notificationsConfig
                                )
                            },
                            onResponse = {
                                handleResponse(
                                    response = it,
                                    notificationsConfig = notificationsConfig
                                )
                                completer.set(Result.success())
                            },
                            onError = {
                                handleError(
                                    exception = it,
                                    notificationsConfig = notificationsConfig
                                )
                                completer.set(Result.failure())
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onStopped() {
        super.onStopped()
        uploadRepository.cancelUpload()
        uploadJob?.cancel()
        uploadJob = null
    }


    /**
     * Private functions
     */
    private fun handleProgressUpdate(progress: Int, notificationsConfig: NotificationsConfig) {
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
            notificationType = NotificationType.Progress(
                progress = progress,
                uploadId = uploadId
            ),
            notificationsConfig = notificationsConfig
        )
    }

    private fun handleResponse(response: Response, notificationsConfig: NotificationsConfig) {

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
        notificationsCreator.removeNotification(NotificationType.Progress())

        //
        if(response.code != responseOk) {
            notificationsCreator.displayNotification(
                notificationType = NotificationType.Error,
                notificationsConfig = notificationsConfig
            )
        }
    }

    private fun handleError(exception: Exception, notificationsConfig: NotificationsConfig) {

        //
        notificationsCreator.removeNotification(NotificationType.Progress())

        if(exception.message == uploadCanceled) {

            //
            notificationsCreator.displayNotification(
                notificationType = NotificationType.Canceled,
                notificationsConfig = notificationsConfig
            )

            //
            eventsPublisher.sendEvent(
                event = UploadEvents.UploadCanceled,
                params = Arguments.createMap().apply {
                    putString(uploadIdParamName, uploadId)
                }
            )

        }else {

            //
            notificationsCreator.displayNotification(
                notificationType = NotificationType.Error,
                notificationsConfig = notificationsConfig
            )

            //
            eventsPublisher.sendEvent(
                event = UploadEvents.UploadError,
                params = Arguments.createMap().apply {
                    putString(uploadIdParamName, uploadId)
                    putString(
                        uploadErrorMessageParamName,
                        exception.message ?: unknownExceptionMessage
                    )
                }
            )
        }
    }


    /**
     *
     */
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