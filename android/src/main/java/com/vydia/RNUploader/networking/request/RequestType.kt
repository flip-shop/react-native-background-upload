package com.vydia.RNUploader.networking.request

private const val rawRequestType = "raw"
private const val multipartRequestType = "multipart"

sealed class RequestType(typeName: String) {
    object Raw: RequestType(rawRequestType)
    object Multipart: RequestType(multipartRequestType)
}


fun requestTypeFromString(type: String?): RequestType {
    return when(type) {
        multipartRequestType -> RequestType.Multipart
        else -> RequestType.Raw
    }
}
