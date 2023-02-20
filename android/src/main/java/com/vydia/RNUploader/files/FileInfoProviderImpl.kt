package com.vydia.RNUploader.files

import android.util.Log
import android.webkit.MimeTypeMap
import com.vydia.RNUploader.emptyString
import java.io.File


private const val TAG = "FileInfoProviderImpl"

class FileInfoProviderImpl: FileInfoProvider {

    override fun getFileInfo(
        path: String?,
        onFileInfoObtained: (obtainedFileInfo: FileInfo) -> Unit,
        onExceptionReceived: (exception: Exception) -> Unit
    ) {
        path?.let { nonNullFilePath ->
            try {
                val fileInfo = FileInfo()
                val file = File(nonNullFilePath)

                fileInfo.fileName = file.name
                fileInfo.extension = MimeTypeMap.getFileExtensionFromUrl(path)
                fileInfo.mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(fileInfo.extension) ?: emptyString
                fileInfo.exists = file.exists() && file.isFile
                fileInfo.fileSize = file.length().toString()

                onFileInfoObtained(fileInfo)

            } catch (exc: Exception) {
                exc.printStackTrace()
                Log.e(TAG, exc.message, exc)
                onExceptionReceived(exc)
            }

        } ?: run {
            onExceptionReceived(Exception(pathNullExceptionMessage))
        }
    }
}