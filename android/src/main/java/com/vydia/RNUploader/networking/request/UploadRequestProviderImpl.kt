package com.vydia.RNUploader.networking.request

import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import java.io.File

class UploadRequestProviderImpl: UploadRequestProvider {

    override fun provideRequest(
        fileInfo: FileInfo,
        requestOptions: UploadRequestOptions,
        onProgress: (Int) -> Unit
    ): Request {
        //
        val requestBody = ProgressRequestBody(
            contentType = fileInfo.mimeType,
            filePath = requestOptions.fileToUploadPath,
            onUploadProgress = { progress ->
                onProgress(progress)
            }
        )

        //
        val requestBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                requestOptions.requestFieldName,
                fileInfo.name,
                requestBody
            )

        //
        requestOptions.params.forEach {
            requestBodyBuilder.addFormDataPart(it.key, it.value)
        }

        //
        val requestBuilder = Request.Builder()
            .url(requestOptions.uploadUrl)
            .post(requestBodyBuilder.build())

        //
        requestOptions.headers.forEach { headersMapEntry ->
            requestBuilder.addHeader(headersMapEntry.key, headersMapEntry.value)
        }

        return requestBuilder.build()
    }


}