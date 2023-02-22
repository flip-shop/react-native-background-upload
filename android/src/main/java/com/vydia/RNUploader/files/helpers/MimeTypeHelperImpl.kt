package com.vydia.RNUploader.files.helpers

import android.webkit.MimeTypeMap
import com.vydia.RNUploader.emptyString

class MimeTypeHelperImpl: MimeTypeHelper {

    override fun getFileExtensionFromUrl(url: String): String =
        MimeTypeMap.getFileExtensionFromUrl(url)
    override fun getMimeTypeFromExtension(extension: String): String =
        MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension) ?: emptyString


}