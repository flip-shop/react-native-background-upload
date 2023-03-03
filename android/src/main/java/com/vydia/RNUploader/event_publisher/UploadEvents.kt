package com.vydia.RNUploader.event_publisher

import com.vydia.RNUploader.helpers.moduleName

const val progressUpdate = "progress"
const val uploadCompleted = "completed"
const val uploadError = "error"
const val uploadCanceled = "canceled"

sealed class UploadEvents(val name: String) {
    object UploadProgress: UploadEvents("$moduleName-$progressUpdate")
    object UploadSuccess: UploadEvents("$moduleName-$uploadCompleted")
    object UploadError: UploadEvents("$moduleName-$uploadError")
    object UploadCanceled: UploadEvents("$moduleName-$uploadCanceled")
}