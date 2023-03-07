package com.vydia.RNUploader

import android.content.IntentFilter
import android.util.Log
import android.util.Log.d
import androidx.work.Configuration
import androidx.work.WorkManager
import com.facebook.react.bridge.*
import com.vydia.RNUploader.di.koinInjector
import com.vydia.RNUploader.files.FileInfo
import com.vydia.RNUploader.files.FileInfoProvider
import com.vydia.RNUploader.files.FileInfoProviderImpl
import com.vydia.RNUploader.helpers.moduleName
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.httpClient.HttpClientOptionsProvider
import com.vydia.RNUploader.networking.httpClient.HttpClientOptionsProviderImpl
import com.vydia.RNUploader.networking.request.options.UploadRequestOptions
import com.vydia.RNUploader.networking.request.options.UploadRequestOptionsProvider
import com.vydia.RNUploader.networking.request.options.UploadRequestOptionsProviderImpl
import com.vydia.RNUploader.notifications.NotificationActionsReceiver
import com.vydia.RNUploader.notifications.config.NotificationsConfig
import com.vydia.RNUploader.notifications.config.NotificationsConfigProvider
import com.vydia.RNUploader.notifications.config.NotificationsConfigProviderImpl
import com.vydia.RNUploader.notifications.data.ACTION_UPLOAD_CANCEL
import com.vydia.RNUploader.notifications.manager.NotificationChannelManager
import com.vydia.RNUploader.notifications.manager.NotificationChannelManagerImpl
import com.vydia.RNUploader.worker.UploadWorker
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject

private const val TAG = "UploaderModule"

class UploaderModule(
  private val reactContext: ReactApplicationContext
): ReactContextBaseJavaModule(reactContext), LifecycleEventListener {

  private var koinStarted = false
  private var fileInfo: FileInfo? = null
  private var httpClientOptions: HttpClientOptions? = null
  private var uploadRequestOptions: UploadRequestOptions? = null
  private var notificationsConfig: NotificationsConfig? = null

  private val fileInfoProvider: FileInfoProvider
    by inject(FileInfoProviderImpl::class.java)

  private val httpClientOptionsProvider: HttpClientOptionsProvider
    by inject(HttpClientOptionsProviderImpl::class.java)

  private val uploadRequestOptionsProvider: UploadRequestOptionsProvider
    by inject(UploadRequestOptionsProviderImpl::class.java)

  private val notificationsConfigProvider: NotificationsConfigProvider
    by inject(NotificationsConfigProviderImpl::class.java)

  private val notificationChannelManager: NotificationChannelManager
    by inject(NotificationChannelManagerImpl::class.java)

  private val notificationActionsReceiver: NotificationActionsReceiver
    by lazy { NotificationActionsReceiver {
      WorkManager.getInstance(reactContext).cancelAllWorkByTag(it) }
    }

  override fun getName() = moduleName


  init {
    if(!koinStarted) {

      startKoin {
        androidContext(reactContext)
        modules(koinInjector)
      }
      koinStarted = true
    }

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
    d(TAG,"startUpload options = $options")

    uploadRequestOptionsProvider.obtainUploadOptions(
      options = options,
      uploadOptionsObtained = { uploadRequestOptions = it },
      uploadOptionsObtainError = { promise.reject(IllegalArgumentException(it)) }
    )

    httpClientOptionsProvider.obtainHttpClientOptions(
      options = options,
      httpOptionsObtained = { httpClientOptions = it },
      wrongOptionType = { promise.reject(IllegalArgumentException(it)) }
    )

    notificationsConfigProvider.provide(
      options = options,
      optionsObtained = {
        notificationsConfig = it
        notificationChannelManager.createChannel()
      },
      errorObtained = { promise.reject(Exception(it)) }
    )

    uploadRequestOptions?.fileToUploadPath?.let { filePath ->
      fileInfoProvider.getFileInfoFromPath(
        path = filePath,
        onFileInfoObtained = { fileInfo = it },
        onExceptionReceived = { promise.reject(Exception(it)) }
      )
    }

    startWorker()
    promise.resolve(uploadRequestOptions?.customUploadId)
  }


  private fun startWorker() {
    if(!WorkManager.isInitialized()) {
      WorkManager.initialize(
        reactContext,
        Configuration.Builder().build()
      )
    }

    fileInfo?.let { fileInfo ->
      uploadRequestOptions?.let { requestOptions ->
        httpClientOptions?.let { httpOptions ->
          notificationsConfig?.let { notificationsConfig ->
            UploadWorker.enqueue(
              WorkManager.getInstance(reactContext),
              fileInfo = fileInfo,
              requestOptions = requestOptions,
              httpClientOptions = httpOptions,
              notificationsConfig = notificationsConfig
            )
          }
        }
      }
    }
  }

  /*
   * Cancels file upload
   * Accepts upload ID as a first argument, this upload will be cancelled
   * Event "cancelled" will be fired when upload is cancelled.
   */
  @ReactMethod
  fun cancelUpload(cancelUploadId: String?, promise: Promise) {
    d(TAG, "cancelUpload!!!")
    if (cancelUploadId !is String) {
      promise.reject(java.lang.IllegalArgumentException("Upload ID must be a string"))
      return
    }
    try {
      WorkManager.getInstance(reactContext).cancelAllWorkByTag(cancelUploadId)
      promise.resolve(true)
    } catch (exc: java.lang.Exception) {
      exc.printStackTrace()
      Log.e(TAG, exc.message, exc)
      promise.reject(exc)
    }
  }

  /*
   * Cancels all file uploads
   */
  @ReactMethod
  fun stopAllUploads(promise: Promise) {
    try {
      WorkManager.getInstance(reactContext).cancelAllWork()
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
