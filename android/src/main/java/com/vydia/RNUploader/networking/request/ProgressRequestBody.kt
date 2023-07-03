package com.vydia.RNUploader.networking.request


import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.*
import java.io.File

private const val BUFFER_SIZE = 1024L

class ProgressRequestBody(
    private val contentType: String,
    private val filePath: String,
    private val onUploadProgress: (progress: Int) -> Unit
) : RequestBody() {

    private val file by lazy {
        File(filePath)
    }

    override fun contentType(): MediaType {
        return contentType.toMediaType()
    }

    override fun contentLength(): Long {
        return file.length()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val fileLength = file.length()
        val source = file.source().buffer()
        var uploaded: Long = 0
        var lastProgressPercentUpdate = 0.0f

        var readCount: Long
        while (source.read(sink.buffer, BUFFER_SIZE).also { readCount = it } != -1L) {
            uploaded += readCount
            sink.flush()

            val progress = (uploaded.toFloat() / fileLength.toFloat()) * 100f
            //prevent publishing too many updates, which slows upload, by checking if the upload has progressed by at least 1 percent
            if (progress - lastProgressPercentUpdate > 1 || progress == 100f) {
                onUploadProgress(progress.toInt())
                lastProgressPercentUpdate = progress
            }
        }

        source.close()
    }
}
