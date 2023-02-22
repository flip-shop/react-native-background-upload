package com.vydia.RNUploader.files.helpers

interface FilesHelper {
    fun getFileName(pathToFile: String): String
    fun fileExists(pathToFile: String): Boolean
    fun getFileSize(pathToFile: String): Long
}