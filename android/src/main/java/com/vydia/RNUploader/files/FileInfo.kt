package com.vydia.RNUploader.files

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.JavaOnlyMap
import com.facebook.react.bridge.WritableMap

private const val fileNameKey = "name"
private const val fileExtensionKey = "extension"
private const val fileMimeTypeKey = "mimeType"
private const val fileSizeKey = "size"
private const val fileExistKey = "exists"

data class FileInfo(
    var name: String = "",
    var extension: String = "",
    var mimeType: String = "",
    var size: String = "",
    var exists: Boolean = false
) {
    fun toArgumentsMap(): WritableMap = JavaOnlyMap().apply {
        putString(fileNameKey, name)
        putString(fileExtensionKey, extension)
        putString(fileMimeTypeKey, mimeType)
        putString(fileSizeKey, size)
        putBoolean(fileExistKey, exists)
    }
}