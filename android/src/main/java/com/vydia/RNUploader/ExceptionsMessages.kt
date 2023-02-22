package com.vydia.RNUploader.files

import com.facebook.react.bridge.ReadableType

const val pathNullExceptionMessage = "Provided path must be not null"
const val headersWrongTypeMessage = "headers must be a hash."
const val notificationWrongTypeMessage = "notification must be a hash."

const val followRedirectsWrongTypeMessage = "followRedirects must be a boolean."
const val followSSLRedirectsWrongTypeMessage = "followSslRedirects must be a boolean."
const val retryOnConnectionFailureWrongTypeMessage = "retryOnConnectionFailure must be a boolean."
const val connectTimeoutWrongTypeMessage = "connectTimeout must be a number."
const val writeTimeoutWrongTypeMessage = "writeTimeout must be a number."
const val readTimeoutWrongTypeMessage = "readTimeout must be a number."
const val requestTypeWrongTypeMessage = "type should be string: raw or multipart."

fun missingKeyMessage(key: String): String = "Missing '$key' field."
fun keyWrongTypeMessage(key: String, type: ReadableType): String = "$key must be a ${type.name}."