package com.vydia.RNUploader.networking.request.options

import com.vydia.RNUploader.emptyString

data class UploadRequestOptions(
    var uploadUrl: String = emptyString,
    var fileToUploadPath: String = emptyString,
    var headers: Map<String, String> = mapOf(),
    var method: String = emptyString,
    var maxRetries: Int = 1,
    var customUploadId: String = emptyString,
    var requestFieldName: String = emptyString,
    var requestType: String = emptyString,
    var params: Map<String, String> = mapOf()
)