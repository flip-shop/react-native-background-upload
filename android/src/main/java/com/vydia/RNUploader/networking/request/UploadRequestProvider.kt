package com.vydia.RNUploader.networking.request

import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import okhttp3.Request

interface UploadRequestProvider {

    fun provideRequest(
        fileInfo: FileInfo,
        requestOptions: UploadRequestOptions,
        onProgress: (Int) -> Unit
    ): Request
}