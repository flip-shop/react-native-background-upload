package com.vydia.RNUploader.networking

import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.httpClient.HttpClientProvider
import com.vydia.RNUploader.networking.httpClient.HttpClientProviderImpl
import com.vydia.RNUploader.networking.request.UploadRequestProvider
import com.vydia.RNUploader.networking.request.UploadRequestProviderImpl
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import okhttp3.*
import org.koin.java.KoinJavaComponent.inject
import java.io.IOException

private val TAG = "UploadRepository"

class UploadRepositoryImpl: UploadRepository {

    private var uploadCall: Call? = null

    private val httpClientProvider: HttpClientProvider
        by inject(HttpClientProviderImpl::class.java)

    private val uploadRequestProvider: UploadRequestProvider
        by inject(UploadRequestProviderImpl::class.java)

    override fun upload(
        fileInfo: FileInfo,
        requestOptions: UploadRequestOptions,
        httpClientOptions: HttpClientOptions,
        onProgress: (Int) -> Unit,
        onResponse: (response: Response) -> Unit,
        onError: (error: IOException) -> Unit,
    ) {

        uploadCall = httpClientProvider.provide(
            options = httpClientOptions
        ).newCall(
            uploadRequestProvider.provideRequest(
                fileInfo = fileInfo,
                requestOptions = requestOptions,
                onProgress = onProgress
            )
        )

        //
        uploadCall?.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                uploadCall = null
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                uploadCall = null
                onResponse(response)
            }
        })
    }

    override fun cancelUpload() {
        uploadCall?.cancel()
    }
}