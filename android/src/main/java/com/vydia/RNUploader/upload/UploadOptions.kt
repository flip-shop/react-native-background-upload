package com.vydia.RNUploader.upload

class UploadOptions(
    var uploadUrl: String? = null,
    var fileToUploadPath: String? = null,
    var method: String? = null,
    var maxRetries: Int = 1,
    var customUploadId: String? = null,
    var requestFieldName: String? = null
)