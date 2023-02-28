package com.vydia.RNUploader.networking

import android.util.Log.d
import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.request.ProgressRequestBody
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

private val TAG = "UploadRepository"
class UploadRepository {

    fun upload(
        fileInfo: FileInfo,
        requestOptions: UploadRequestOptions,
        httpClientOptions: HttpClientOptions,
        onProgress: (Int) -> Unit,
        onResponse: (response: Response) -> Unit,
        onError: (error: IOException) -> Unit,
    ) {

        try {

            //
            val requestBody = ProgressRequestBody(
                contentType = fileInfo.mimeType.toMediaType(),
                file = File(requestOptions.fileToUploadPath),
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

            val httpClient = OkHttpClient.Builder()
                .followRedirects(httpClientOptions.followRedirects)
                .followSslRedirects(httpClientOptions.followSslRedirects)
                .retryOnConnectionFailure(httpClientOptions.retryOnConnectionFailure)
                .connectTimeout(httpClientOptions.connectTimeout, TimeUnit.SECONDS)
                .writeTimeout(httpClientOptions.writeTimeout, TimeUnit.SECONDS)
                .readTimeout(httpClientOptions.readTimeout, TimeUnit.SECONDS)
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                ).build()

            httpClient.newCall(requestBuilder.build()).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    d(TAG,"upload onFailure ${e.message}")
                    onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    d(TAG,"upload onResponse code: ${response.code}")
                    d(TAG,"upload onResponse body: ${response.body}")
                    onResponse(response)
                }
            })

        } catch (e: IOException) {
            d(TAG,"upload exception ${e.message}")
            onError(e)
        }
    }
}