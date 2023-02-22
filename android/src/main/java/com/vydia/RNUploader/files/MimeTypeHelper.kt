package com.vydia.RNUploader.files

interface MimeTypeHelper {

    fun getFileExtensionFromUrl(url: String): String
    fun getMimeTypeFromExtension(extension: String): String

}