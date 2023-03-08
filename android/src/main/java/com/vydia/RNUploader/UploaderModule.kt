package com.vydia.RNUploader

import android.content.IntentFilter
import android.util.Log
import android.util.Log.d
import com.facebook.react.bridge.*
import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.files.FileInfoProvider
import com.vydia.RNUploader.helpers.*
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.httpClient.HttpClientOptionsProvider
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import com.vydia.RNUploader.networking.request.options.UploadRequestOptionsProvider
import com.vydia.RNUploader.notifications.NotificationActionsReceiver
import com.vydia.RNUploader.notifications.config.NotificationsConfig
import com.vydia.RNUploader.notifications.config.NotificationsConfigProvider
import com.vydia.RNUploader.notifications.data.ACTION_UPLOAD_CANCEL
import com.vydia.RNUploader.notifications.manager.NotificationChannelManager
import com.vydia.RNUploader.worker.UploadWorkerManager

private const val TAG = "UploaderModule"

class UploaderModule(
  private val reactContext: ReactApplicationContext,
  private val fileInfoProvider: FileInfoProvider,
  private val httpClientOptionsProvider: HttpClientOptionsProvider,
  private val uploadRequestOptionsProvider: UploadRequestOptionsProvider,
  private val notificationsConfigProvider: NotificationsConfigProvider,
  private val notificationChannelManager: NotificationChannelManager,
  private val uploadWorkerManager: UploadWorkerManager
): ReactContextBaseJavaModule(reactContext), LifecycleEventListener {

  private val notificationActionsReceiver: NotificationActionsReceiver
    by lazy { NotificationActionsReceiver { uploadWorkerManager.cancelWorker(it) } }

  override fun getName() = moduleName

  init {
    reactContext.addLifecycleEventListener(this)
  }

  /*
  Gets file information for the path specified.  Example valid path is: /storage/extSdCard/DCIM/Camera/20161116_074726.mp4
  Returns an object such as: {extension: "mp4", size: "3804316", exists: true, mimeType: "video/mp4", name: "20161116_074726.mp4"}
   */
  @ReactMethod
  fun getFileInfo(path: String?, promise: Promise) {
    fileInfoProvider.getFileInfoFromPath(
      path = path,
      onFileInfoObtained = { promise.resolve(it.toArgumentsMap()) },
      onExceptionReceived = { promise.reject(it) }
    )
  }

  /*
   * Starts a file upload.
   * Returns a promise with the string ID of the upload.
   */
  @ReactMethod
  fun startUpload(options: ReadableMap, promise: Promise) {

    var uploadRequestOptions: UploadRequestOptions? = null
    var httpClientOptions: HttpClientOptions? = null
    var notificationsConfig: NotificationsConfig? = null
    var fileInfo: FileInfo? = null

    uploadRequestOptionsProvider.obtainUploadOptions(
      options = options,
      uploadOptionsObtained = { uploadRequestOptions = it },
      uploadOptionsObtainError = { promise.reject(IllegalArgumentException(it)) }
    )

    if(uploadRequestOptions == null) {
      return
    }

    httpClientOptionsProvider.obtainHttpClientOptions(
      options = options,
      httpOptionsObtained = { httpClientOptions = it },
      wrongOptionType = { promise.reject(IllegalArgumentException(it)) }
    )

    if(httpClientOptions == null) {
      return
    }

    notificationsConfigProvider.provide(
      options = options,
      optionsObtained = {
        notificationsConfig = it
        notificationChannelManager.createChannel()
      },
      errorObtained = { promise.reject(Exception(it)) }
    )

    if(notificationsConfig == null) {
      return
    }

    uploadRequestOptions?.fileToUploadPath?.let { filePath ->
      fileInfoProvider.getFileInfoFromPath(
        path = filePath,
        onFileInfoObtained = { fileInfo = it },
        onExceptionReceived = { promise.reject(Exception(it)) }
      )
    }

    if(fileInfo == null) {
      return
    }

    fileInfo?.let { info ->
      uploadRequestOptions?.let { requestOptions ->
        httpClientOptions?.let { httpOptions ->
          notificationsConfig?.let { config ->
            uploadWorkerManager.startWorker(
              fileInfo = info,
              requestOptions = requestOptions,
              httpClientOptions = httpOptions,
              notificationsConfig = config
            )
          }
        }
      }
    }

    promise.resolve(uploadRequestOptions?.customUploadId)
  }

  /*
   * Cancels file upload
   * Accepts upload ID as a first argument, this upload will be cancelled
   * Event "cancelled" will be fired when upload is cancelled.
   */
  @ReactMethod
  fun cancelUpload(cancelUploadId: String?, promise: Promise) {
    if (cancelUploadId !is String) {
      promise.reject(java.lang.IllegalArgumentException("Upload ID must be a string"))
      return
    }
    try {
      uploadWorkerManager.cancelWorker(cancelUploadId)
      promise.resolve(true)
    } catch (exc: java.lang.Exception) {
      exc.printStackTrace()
      promise.reject(exc)
    }
  }

  /*
   * Cancels all file uploads
   */
  @ReactMethod
  fun stopAllUploads(promise: Promise) {
    try {
      uploadWorkerManager.cancelAllWorkers()
      promise.resolve(true)
    } catch (exc: java.lang.Exception) {
      exc.printStackTrace()
      Log.e(TAG, exc.message, exc)
      promise.reject(exc)
    }
  }

  override fun onHostResume() {
    d(TAG,"onHostResume")
    reactContext.registerReceiver(
      notificationActionsReceiver,
      IntentFilter(ACTION_UPLOAD_CANCEL)
    )
  }

  override fun onHostPause() {
    d(TAG,"onHostPause")
  }

  override fun onHostDestroy() {
    d(TAG,"onHostDestroy")
    reactContext.unregisterReceiver(notificationActionsReceiver)
  }
}
