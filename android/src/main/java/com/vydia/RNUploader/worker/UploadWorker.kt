package com.vydia.RNUploader.worker

import android.content.Context
import android.util.Log.d
import androidx.work.*
import com.vydia.RNUploader.TAG
import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.files.helpers.deserializeFromJson
import com.vydia.RNUploader.files.helpers.serializeToJson
import com.vydia.RNUploader.networking.UploadRepository
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class UploadWorker(
    appContext: Context,
    params: WorkerParameters
): Worker(appContext, params) {

    private var uploadJob: Job? = null

    private val uploadRepository: UploadRepository by lazy {
        UploadRepository()
    }

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

        if(uploadFileInfo == null || requestOptions == null || httpClientOptions == null) {
            return Result.failure()
        }

        uploadJob?.let { job ->
            CoroutineScope(Dispatchers.IO + job).launch {
                uploadRepository.upload(
                    fileInfo = uploadFileInfo,
                    requestOptions = requestOptions,
                    httpClientOptions = httpClientOptions,
                    onProgress = { progress ->
                        d(TAG, "upload progress update: $progress")
                    },
                    onResponse = {
                        d(TAG, "upload onResponse: ${it.body}")
                    },
                    onError = {
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
        private const val uniqueWorkName = "FileUploadWork-"

        fun enqueue(
            workManager: WorkManager,
            fileInfo: FileInfo,
            requestOptions: UploadRequestOptions,
            httpClientOptions: HttpClientOptions
        ) {
            val inputData = Data.Builder()
                .putString(fileInfoKey, serializeToJson(fileInfo))
                .putString(requestOptionsKey, serializeToJson(requestOptions))
                .putString(httpClientOptionsKey, serializeToJson(httpClientOptions))
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