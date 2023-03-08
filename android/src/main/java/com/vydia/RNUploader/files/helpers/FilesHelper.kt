package com.vydia.RNUploader.files.helpers

import java.io.File
import java.io.InputStream

interface FilesHelper {
    fun getFileName(pathToFile: String): String
    fun fileExists(pathToFile: String): Boolean
    fun getFileSize(pathToFile: String): Long
    fun copyFile(inputStream: InputStream, outFile: File)
}