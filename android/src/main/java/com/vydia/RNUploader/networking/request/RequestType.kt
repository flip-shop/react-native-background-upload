package com.vydia.RNUploader.networking.request

private const val rawRequestType = "RAW"
private const val postRequestType = "POST"
private const val deleteRequestType = "DELETE"
private const val putRequestType = "PUT"
private const val getRequestType = "GET"
private const val multipartRequestType = "MULTIPART"

sealed class RequestType(typeName: String) {
    object Raw: RequestType(rawRequestType)
    object Post: RequestType(postRequestType)
    object Delete: RequestType(deleteRequestType)
    object Put: RequestType(putRequestType)
    object Get: RequestType(getRequestType)
    object Multipart: RequestType(multipartRequestType)
}


fun requestTypeFromString(type: String?): RequestType {
    return when(type) {
        postRequestType -> RequestType.Post
        deleteRequestType -> RequestType.Delete
        putRequestType -> RequestType.Put
        getRequestType -> RequestType.Get
        multipartRequestType -> RequestType.Multipart
        else -> RequestType.Raw
    }
}
