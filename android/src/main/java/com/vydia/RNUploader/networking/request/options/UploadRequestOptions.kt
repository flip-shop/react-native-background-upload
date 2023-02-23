package com.vydia.RNUploader.networking.request.options

import com.vydia.RNUploader.emptyString
import com.vydia.RNUploader.networking.request.RequestType

class UploadRequestOptions(
    var uploadUrl: String = emptyString,
    var fileToUploadPath: String = emptyString,
    var headers: Map<String, String> = mapOf(),
    var method: String = emptyString,
    var maxRetries: Int = 1,
    var customUploadId: String = emptyString,
    var requestFieldName: String = emptyString,
    var requestType: RequestType = RequestType.Raw,
)