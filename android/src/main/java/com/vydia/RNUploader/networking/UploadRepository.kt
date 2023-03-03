package com.vydia.RNUploader.networking

import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import okhttp3.Response
import java.io.IOException

interface UploadRepository {
    fun upload(
        fileInfo: FileInfo,
        requestOptions: UploadRequestOptions,
        httpClientOptions: HttpClientOptions,
        onProgress: (Int) -> Unit,
        onResponse: (response: Response) -> Unit,
        onError: (error: IOException) -> Unit
    )

    fun cancelUpload()
}