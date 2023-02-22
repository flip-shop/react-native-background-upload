package com.vydia.RNUploader.files.helpers

import java.io.File

class FilesHelperImpl: FilesHelper {

    override fun getFileName(pathToFile: String): String {
        return File(pathToFile).name
    }

    override fun fileExists(pathToFile: String): Boolean {
        val file = File(pathToFile)
        return (file.exists() && file.isFile)
    }

    override fun getFileSize(pathToFile: String): Long {
        return File(pathToFile).length()
    }
}