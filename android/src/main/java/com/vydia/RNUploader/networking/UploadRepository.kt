package com.vydia.RNUploader.networking

import android.content.Context
import android.net.Uri
import android.util.Log.d
import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.files.helpers.FilesHelper
import com.vydia.RNUploader.files.helpers.FilesHelperImpl
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.request.ProgressRequestBody
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

private val TAG = "UploadRepository"
class UploadRepository {

    private val filesHelper: FilesHelper by lazy {
        FilesHelperImpl()
    }

    fun upload(
        context: Context,
        fileInfo: FileInfo,
        requestOptions: UploadRequestOptions,
        httpClientOptions: HttpClientOptions,
        onProgress: (Int) -> Unit,
        onResponse: (response: Response) -> Unit,
        onError: (error: IOException) -> Unit,
    ) {

        val tempFile = File(context.cacheDir, fileInfo.name)

        try {
            val inputStream = context.contentResolver.openInputStream(
                Uri.parse(requestOptions.fileToUploadPath)
            )

            inputStream?.let { stream ->
                filesHelper.copyFile(stream, tempFile)

                stream.close()

                val requestBody = ProgressRequestBody(
                    contentType = fileInfo.mimeType.toMediaType(),
                    file = tempFile,
                    onUploadProgress = { progress ->
                        onProgress(progress)
                    }
                )

                // create file part of upload form
                val filePart = MultipartBody.Part.createFormData(
                    name = requestOptions.requestFieldName,
                    filename = tempFile.name,
                    body = requestBody
                )

                // create body builder
                val bodyBuilder = MultipartBody.Builder()

                // add file as a param
                bodyBuilder.addPart(filePart)

                // add other params
                requestOptions.params.forEach {
                    bodyBuilder.addFormDataPart(it.key, it.value)
                }

                val requestBuilder = Request.Builder()
                    .url(requestOptions.uploadUrl)
                    .method(
                        requestOptions.method,
                        bodyBuilder.build()
                    )

                requestOptions.headers.forEach { headersMapEntry ->
                    requestBuilder.addHeader(headersMapEntry.key, headersMapEntry.value)
                }

                val request = requestBuilder.build()

                val httpClient = OkHttpClient.Builder()
                    .followRedirects(httpClientOptions.followRedirects)
                    .followSslRedirects(httpClientOptions.followSslRedirects)
                    .retryOnConnectionFailure(httpClientOptions.retryOnConnectionFailure)
                    .connectTimeout(httpClientOptions.connectTimeout, TimeUnit.SECONDS)
                    .writeTimeout(httpClientOptions.writeTimeout, TimeUnit.SECONDS)
                    .readTimeout(httpClientOptions.readTimeout, TimeUnit.SECONDS)
                    .build()

                httpClient.newCall(request).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        d(TAG,"upload onFailure ${e.message}")
                        onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        onResponse(response)
                    }
                })
            }
        } catch (e: IOException) {
            d(TAG,"upload exception ${e.message}")
            onError(e)
        } finally {
            d(TAG,"upload finally")
            tempFile.delete()
        }
    }
}