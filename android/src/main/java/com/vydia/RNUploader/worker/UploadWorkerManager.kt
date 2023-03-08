package com.vydia.RNUploader.worker

import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import com.vydia.RNUploader.notifications.config.NotificationsConfig

interface UploadWorkerManager {
    fun startWorker(
        fileInfo: FileInfo,
        requestOptions: UploadRequestOptions,
        httpClientOptions: HttpClientOptions,
        notificationsConfig: NotificationsConfig
    )
    fun cancelWorker(tag: String)
    fun cancelAllWorkers()

}