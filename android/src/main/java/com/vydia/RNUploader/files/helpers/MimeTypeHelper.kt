package com.vydia.RNUploader.files.helpers

interface MimeTypeHelper {

    fun getFileExtensionFromUrl(url: String): String
    fun getMimeTypeFromExtension(extension: String): String

}