package com.vydia.RNUploader.files.helpers

import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.InputStream
import kotlin.math.sin

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

    override fun copyFile(inputStream: InputStream, outFile: File) {
        inputStream.source().buffer().use { source ->
            outFile.sink().buffer().use { sink ->
                sink.writeAll(source)
            }
        }
    }
}