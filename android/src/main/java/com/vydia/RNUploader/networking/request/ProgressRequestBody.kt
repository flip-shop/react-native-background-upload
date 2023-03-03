package com.vydia.RNUploader.networking.request


import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.File
import java.io.FileInputStream

class ProgressRequestBody(
    private val contentType: MediaType,
    private val file: File,
    private val onUploadProgress: (progress: Int) -> Unit
) : RequestBody() {

    private var numWriteToCalls = 0

    override fun contentType(): MediaType {
        return contentType
    }

    override fun contentLength(): Long {
        return file.length()
    }

    override fun writeTo(sink: BufferedSink) {
        numWriteToCalls++

        val fileLength = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val `in` = FileInputStream(file)
        var uploaded: Long = 0

        `in`.use {
            var read: Int
            var lastProgressPercentUpdate = 0.0f
            read = `in`.read(buffer)
            while (read != -1) {

                uploaded += read.toLong()
                sink.write(buffer, 0, read)
                read = `in`.read(buffer)

                // when using HttpLoggingInterceptor it calls writeTo and passes data into a local buffer just for logging purposes.
                // the second call to write to is the progress we actually want to track
                if (numWriteToCalls > getIgnoreFirstNumberOfWriteToCalls()) {
                    val progress = (uploaded.toFloat() / fileLength.toFloat()) * 100f
                    //prevent publishing too many updates, which slows upload, by checking if the upload has progressed by at least 1 percent
                    if (progress - lastProgressPercentUpdate > 1 || progress == 100f) {
                        onUploadProgress(progress.toInt())
                        lastProgressPercentUpdate = progress
                    }
                }
            }
        }
    }

    private fun getIgnoreFirstNumberOfWriteToCalls(): Int = 1



}