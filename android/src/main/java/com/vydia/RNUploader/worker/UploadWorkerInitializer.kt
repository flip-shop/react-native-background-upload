package com.vydia.RNUploader.worker

import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import com.vydia.RNUploader.notifications.config.NotificationsConfig

interface UploadWorkerInitializer {

    fun startWorker(
        fileInfo: FileInfo,
        requestOptions: UploadRequestOptions,
        httpClientOptions: HttpClientOptions,
        notificationsConfig: NotificationsConfig
    )

}