package com.vydia.RNUploader.worker

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import com.vydia.RNUploader.notifications.config.NotificationsConfig

class UploadWorkerInitializerImpl(
    private val context: Context
): UploadWorkerInitializer {

    override fun startWorker(
        fileInfo: FileInfo,
        requestOptions: UploadRequestOptions,
        httpClientOptions: HttpClientOptions,
        notificationsConfig: NotificationsConfig
    ) {
        if(!WorkManager.isInitialized()) {
            WorkManager.initialize(
                context,
                Configuration.Builder().build()
            )
        }

        UploadWorker.enqueue(
            workManager = WorkManager.getInstance(context),
            fileInfo = fileInfo,
            requestOptions = requestOptions,
            httpClientOptions = httpClientOptions,
            notificationsConfig = notificationsConfig
        )
    }
}