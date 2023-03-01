package com.vydia.RNUploader.event_publisher

import com.vydia.RNUploader.moduleName

const val progressUpdate = "progress"
const val uploadCompleted = "completed"
const val uploadError = "error"

sealed class UploadEvents(val name: String) {
    object UploadProgress: UploadEvents("$moduleName-$progressUpdate")
    object UploadSuccess: UploadEvents("$moduleName-$uploadCompleted")
    object UploadError: UploadEvents("$moduleName-$uploadError")
}