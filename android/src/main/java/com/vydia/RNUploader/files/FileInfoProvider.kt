package com.vydia.RNUploader.files

import com.facebook.react.bridge.ReadableMap

interface FileInfoProvider {
    fun getFileInfoFromPath(
        path: String?,
        onFileInfoObtained: (obtainedFileInfo: FileInfo) -> Unit = {},
        onExceptionReceived: (exception: Exception) -> Unit = {}
    )
}