package com.vydia.RNUploader

import com.facebook.react.bridge.*

abstract class RNUPloaderSpec internal constructor(context: ReactApplicationContext) :
  ReactContextBaseJavaModule(context) {
    abstract fun getFileInfo(path: String?, promise: Promise);
    abstract fun startUpload(options: ReadableMap, promise: Promise);
    abstract fun cancelUpload(cancelUploadId: String?, promise: Promise);
    abstract fun stopAllUploads(promise: Promise);
  }