package com.vydia.RNUploader.networking.request.options

import com.facebook.react.bridge.ReadableMap
import com.vydia.RNUploader.files.requestTypeWrongTypeMessage
import com.vydia.RNUploader.networking.request.RequestType
import com.vydia.RNUploader.networking.request.requestTypeFromString

class RequestOptionsProviderImpl: RequestOptionsProvider {

    override fun obtainRequestOptions(
        options: ReadableMap,
        requestOptionsObtained: (RequestOptions) -> Unit,
        onErrorObtained: (String) -> Unit
    ) {
        val requestOptions = RequestOptions()

        if (options.hasKey(typeKey)) {

            options.getString(typeKey)?.let { type ->
                requestOptions.type = requestTypeFromString(options.getString(type))
            } ?: run {
                onErrorObtained(requestTypeWrongTypeMessage)
                return
            }

            if (requestOptions.type != RequestType.Raw && requestOptions.type != RequestType.Multipart) {
                onErrorObtained(requestTypeWrongTypeMessage)
                return
            }
        }
    }


}