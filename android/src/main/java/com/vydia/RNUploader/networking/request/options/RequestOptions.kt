package com.vydia.RNUploader.networking.request.options

import com.vydia.RNUploader.networking.request.RequestType

data class RequestOptions(
    var type: RequestType = RequestType.Raw
)