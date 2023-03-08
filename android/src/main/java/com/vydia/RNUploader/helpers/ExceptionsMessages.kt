package com.vydia.RNUploader.helpers

import com.facebook.react.bridge.ReadableType

const val pathNullExceptionMessage = "Provided path must be not null"

const val followRedirectsWrongTypeMessage = "followRedirects must be a boolean."
const val followSSLRedirectsWrongTypeMessage = "followSslRedirects must be a boolean."
const val retryOnConnectionFailureWrongTypeMessage = "retryOnConnectionFailure must be a boolean."
const val connectTimeoutWrongTypeMessage = "connectTimeout must be a number."
const val writeTimeoutWrongTypeMessage = "writeTimeout must be a number."
const val readTimeoutWrongTypeMessage = "readTimeout must be a number."
const val requestTypeWrongTypeMessage = "type should be string: raw or multipart."

const val emitEventErrorJsModuleNull = "sendEvent() failed due getJSModule == null!"
const val unknownExceptionMessage = "Unknown exception"
const val uploadCanceled = "Canceled"

const val fileInfoNullExceptionsMessage = "fileInfo must not be null"
const val uploadRequestOptionsNullExceptionsMessage = "uploadRequestOptions must not be null"
const val httpClientOptionsExceptionsMessage = "httpClientOptions must not be null"
const val notificationsConfigExceptionsMessage = "notificationsConfig must not be null"

fun missingKeyMessage(key: String): String = "Missing '$key' field."
fun keyWrongTypeMessage(key: String, type: ReadableType): String = "'$key' must be a ${type.name}."