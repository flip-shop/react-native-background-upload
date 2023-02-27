package com.vydia.RNUploader.networking.request.options

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.vydia.RNUploader.ReadableMapFieldState
import com.vydia.RNUploader.emptyString
import com.vydia.RNUploader.files.*
import com.vydia.RNUploader.networking.request.RequestType
import com.vydia.RNUploader.networking.request.requestTypeFromString
import com.vydia.RNUploader.obtainFieldState
import com.vydia.RNUploader.toMap

class UploadRequestOptionsProviderImpl: UploadRequestOptionsProvider {

    /**
     * Create [UploadRequestOptions] object based on the values passed in [options] Map
     * If type is wrong or required field is missing return the specific error message
     **/

    override fun obtainUploadOptions(
        options: ReadableMap,
        uploadOptionsObtained: (UploadRequestOptions) -> Unit,
        uploadOptionsObtainError: (String) -> Unit
    ) {
        val uploadRequestOptions = UploadRequestOptions()

        for(key in arrayOf(
            urlKey, pathKey, headersKey, methodKey,
            maxRetriesKey, customUploadIdKey, requestFieldNameKey, requestTypeKey, parametersKey
        )
        ) {
            when(
                obtainFieldState(
                    map = options,
                    fieldNameKey = key,
                    requiredFieldType = when(key) {
                        maxRetriesKey -> ReadableType.Number
                        headersKey -> ReadableType.Map
                        parametersKey -> ReadableType.Map
                        else -> ReadableType.String
                    }
                )
            ) {
                ReadableMapFieldState.WrongType -> {
                    uploadOptionsObtainError(
                        keyWrongTypeMessage(
                            key,
                            when(key) {
                                maxRetriesKey -> ReadableType.Number
                                else -> ReadableType.String
                            }
                        )
                    )
                    return
                }
                ReadableMapFieldState.NotExists -> {
                    uploadOptionsObtainError(missingKeyMessage(key))
                    return
                }
                ReadableMapFieldState.Correct -> when(key) {

                    urlKey -> uploadRequestOptions.uploadUrl =
                        options.getString(urlKey) ?:  emptyString

                    pathKey -> uploadRequestOptions.fileToUploadPath =
                        options.getString(pathKey) ?: emptyString

                    headersKey -> uploadRequestOptions.headers =
                        options.getMap(headersKey)?.toMap() ?: mapOf()

                    methodKey -> uploadRequestOptions.method =
                        options.getString(methodKey) ?: emptyString

                    maxRetriesKey -> uploadRequestOptions.maxRetries = options.getInt(maxRetriesKey)

                    customUploadIdKey -> uploadRequestOptions.customUploadId =
                        options.getString(customUploadIdKey) ?: emptyString

                    requestFieldNameKey -> uploadRequestOptions.requestFieldName =
                        options.getString(requestFieldNameKey) ?: emptyString

                    requestTypeKey -> uploadRequestOptions.requestType =
                        requestTypeFromString(options.getString(requestTypeKey))

                    parametersKey -> uploadRequestOptions.params =
                        options.getMap(parametersKey)?.toMap() ?: mapOf()
                }
            }
        }

        uploadOptionsObtained(uploadRequestOptions)
    }
}
