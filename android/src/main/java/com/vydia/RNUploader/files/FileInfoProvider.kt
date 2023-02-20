package com.vydia.RNUploader.files

interface FileInfoProvider {
    fun getFileInfo(
        path: String?,
        onFileInfoObtained: (obtainedFileInfo: FileInfo) -> Unit = {},
        onExceptionReceived: (exception: Exception) -> Unit = {}
    )
}