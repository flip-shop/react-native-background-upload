package com.vydia.RNUploader.files

import com.vydia.RNUploader.files.helpers.FilesHelper
import com.vydia.RNUploader.files.helpers.MimeTypeHelper
import com.vydia.RNUploader.helpers.pathNullExceptionMessage


private const val TAG = "FileInfoProviderImpl"


class FileInfoProviderImpl(
    private val mimeTypeHelper: MimeTypeHelper,
    private val filesHelper: FilesHelper
): FileInfoProvider {

    override fun getFileInfoFromPath(
        path: String?,
        onFileInfoObtained: (obtainedFileInfo: FileInfo) -> Unit,
        onExceptionReceived: (exception: Exception) -> Unit
    ) {
        path?.let { nonNullFilePath ->
            try {
                val fileInfo = FileInfo()

                fileInfo.name = filesHelper.getFileName(nonNullFilePath)
                fileInfo.extension = mimeTypeHelper.getFileExtensionFromUrl(nonNullFilePath)
                fileInfo.mimeType = mimeTypeHelper.getMimeTypeFromExtension(fileInfo.extension)
                fileInfo.exists = filesHelper.fileExists(nonNullFilePath)
                fileInfo.size = filesHelper.getFileSize(nonNullFilePath).toString()

                onFileInfoObtained(fileInfo)

            } catch (exc: Exception) {
                exc.printStackTrace()
                //Log.e(TAG, exc.message, exc)
                onExceptionReceived(exc)
            }

        } ?: run {
            onExceptionReceived(Exception(pathNullExceptionMessage))
        }
    }
}