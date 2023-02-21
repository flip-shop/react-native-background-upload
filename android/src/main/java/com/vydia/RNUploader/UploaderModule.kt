package com.vydia.RNUploader

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.facebook.react.bridge.*
import com.vydia.RNUploader.files.FileInfoProvider
import com.vydia.RNUploader.files.FileInfoProviderImpl
import com.vydia.RNUploader.networking.httpClient.HttpClientOptions
import com.vydia.RNUploader.networking.httpClient.HttpClientOptionsProvider
import com.vydia.RNUploader.networking.httpClient.HttpClientOptionsProviderImpl
import com.vydia.RNUploader.networking.request.options.RequestOptions
import com.vydia.RNUploader.networking.request.options.RequestOptionsProvider
import com.vydia.RNUploader.networking.request.options.RequestOptionsProviderImpl
import com.vydia.RNUploader.upload.UploadOptionsValidator
import com.vydia.RNUploader.upload.UploadOptionsValidatorImpl

private const val TAG = "UploaderBridge"
private const val moduleName = "RNFileUploader"

class UploaderModule(
  private val reactContext: ReactApplicationContext
): ReactContextBaseJavaModule(reactContext), LifecycleEventListener {

  private var httpClientOptions: HttpClientOptions? = null
  private var requestOptions: RequestOptions? = null

  private val fileInfoProvider: FileInfoProvider
    by lazy { FileInfoProviderImpl() }

  private val httpClientOptionsProvider: HttpClientOptionsProvider
    by lazy { HttpClientOptionsProviderImpl() }

  private val requestOptionsProvider: RequestOptionsProvider
    by lazy { RequestOptionsProviderImpl() }

  private val uploadOptionsValidator: UploadOptionsValidator by lazy { UploadOptionsValidatorImpl() }

  private var notificationChannelID = "BackgroundUploadChannel"
  private var isGlobalRequestObserver = false

  override fun getName() = moduleName

  /*
  Gets file information for the path specified.  Example valid path is: /storage/extSdCard/DCIM/Camera/20161116_074726.mp4
  Returns an object such as: {extension: "mp4", size: "3804316", exists: true, mimeType: "video/mp4", name: "20161116_074726.mp4"}
   */
  @ReactMethod
  fun getFileInfo(path: String?, promise: Promise) {
    fileInfoProvider.getFileInfo(
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

    uploadOptionsValidator.validate(
      uploadOptions = options,
      onMissingArgument = {
        promise.reject(IllegalArgumentException(it))
      },
      onValidationSuccess = {
        httpClientOptionsProvider.obtainHttpClientOptions(
          options = options,
          httpOptionsObtained = { providedOptions ->
            httpClientOptions = providedOptions
          },
          wrongOptionType = { exceptionMessage ->
            promise.reject(IllegalArgumentException(exceptionMessage))
          }
        )
      }
    )

    //configureUploadServiceHTTPStack(options, promise)

    requestOptionsProvider.obtainRequestOptions(
      options = options,
      requestOptionsObtained = { obtainedOptions ->
        requestOptions = obtainedOptions
      },
      onErrorObtained = { message ->
        promise.reject(IllegalArgumentException(message))
      }
    )



    //todo continue...

    val notification: WritableMap = WritableNativeMap()
    notification.putBoolean("enabled", true)
    if (options.hasKey("notification")) {
      notification.merge(options.getMap("notification")!!)
    }

    val application = reactContext.applicationContext as Application

    reactContext.addLifecycleEventListener(this)

    if (notification.hasKey("notificationChannel")) {
      notificationChannelID = notification.getString("notificationChannel")!!
    }

    createNotificationChannel()

    //initialize(application, notificationChannelID, BuildConfig.DEBUG)

    /*
    if(!isGlobalRequestObserver) {
      isGlobalRequestObserver = true
      GlobalRequestObserver(application, GlobalRequestObserverDelegate(reactContext))
    }*/

    val url = options.getString("url")
    val filePath = options.getString("path")
    val method = if (options.hasKey("method") && options.getType("method") == ReadableType.String) options.getString("method") else "POST"
    val maxRetries = if (options.hasKey("maxRetries") && options.getType("maxRetries") == ReadableType.Number) options.getInt("maxRetries") else 2
    val customUploadId = if (options.hasKey("customUploadId") && options.getType("method") == ReadableType.String) options.getString("customUploadId") else null
    try {
      val request = if (requestType == "raw") {
        BinaryUploadRequest(this.reactApplicationContext, url!!)
                .setFileToUpload(filePath!!)
      } else {
        if (!options.hasKey("field")) {
          promise.reject(java.lang.IllegalArgumentException("field is required field for multipart type."))
          return
        }
        if (options.getType("field") != ReadableType.String) {
          promise.reject(java.lang.IllegalArgumentException("field must be string."))
          return
        }
        MultipartUploadRequest(this.reactApplicationContext, url!!)
                .addFileToUpload(filePath!!, options.getString("field")!!)
      }
      request.setMethod(method!!)
              .setMaxRetries(maxRetries)
      if (notification.getBoolean("enabled")) {
        val notificationConfig = UploadNotificationConfig(
                notificationChannelId = notificationChannelID,
                isRingToneEnabled = notification.hasKey("enableRingTone") && notification.getBoolean("enableRingTone"),
                progress = UploadNotificationStatusConfig(
                        title = if (notification.hasKey("onProgressTitle")) notification.getString("onProgressTitle")!! else "",
                        message = if (notification.hasKey("onProgressMessage")) notification.getString("onProgressMessage")!! else ""
                ),
                success = UploadNotificationStatusConfig(
                        title = if (notification.hasKey("onCompleteTitle")) notification.getString("onCompleteTitle")!! else "",
                        message = if (notification.hasKey("onCompleteMessage")) notification.getString("onCompleteMessage")!! else "",
                        autoClear = notification.hasKey("autoClear") && notification.getBoolean("autoClear")
                ),
                error = UploadNotificationStatusConfig(
                        title = if (notification.hasKey("onErrorTitle")) notification.getString("onErrorTitle")!! else "",
                        message = if (notification.hasKey("onErrorMessage")) notification.getString("onErrorMessage")!! else ""
                ),
                cancelled = UploadNotificationStatusConfig(
                        title = if (notification.hasKey("onCancelledTitle")) notification.getString("onCancelledTitle")!! else "",
                        message = if (notification.hasKey("onCancelledMessage")) notification.getString("onCancelledMessage")!! else ""
                )
        )
        request.setNotificationConfig { _, _ ->
          notificationConfig
        }
      }
      if (options.hasKey("parameters")) {
        if (requestType == "raw") {
          promise.reject(java.lang.IllegalArgumentException("Parameters supported only in multipart type"))
          return
        }
        val parameters = options.getMap("parameters")
        val keys = parameters!!.keySetIterator()
        while (keys.hasNextKey()) {
          val key = keys.nextKey()
          if (parameters.getType(key) != ReadableType.String) {
            promise.reject(java.lang.IllegalArgumentException("Parameters must be string key/values. Value was invalid for '$key'"))
            return
          }
          request.addParameter(key, parameters.getString(key)!!)
        }
      }
      if (options.hasKey("headers")) {
        val headers = options.getMap("headers")
        val keys = headers!!.keySetIterator()
        while (keys.hasNextKey()) {
          val key = keys.nextKey()
          if (headers.getType(key) != ReadableType.String) {
            promise.reject(java.lang.IllegalArgumentException("Headers must be string key/values.  Value was invalid for '$key'"))
            return
          }
          request.addHeader(key, headers.getString(key)!!)
        }
      }
      if (customUploadId != null)
        request.setUploadID(customUploadId)

      val uploadId = request.startUpload()
      promise.resolve(uploadId)
    } catch (exc: java.lang.Exception) {
      exc.printStackTrace()
      Log.e(TAG, exc.message, exc)
      promise.reject(exc)
    }
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
      UploadService.stopUpload(cancelUploadId)
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
      UploadService.stopAllUploads()
      promise.resolve(true)
    } catch (exc: java.lang.Exception) {
      exc.printStackTrace()
      Log.e(TAG, exc.message, exc)
      promise.reject(exc)
    }
  }

  private fun configureUploadServiceHTTPStack(options: ReadableMap, promise: Promise) {

    httpClientOptionsProvider.obtainHttpClientOptions(
      options = options,
      httpOptionsObtained = {
        //todo
        /**
         OkHttpClient().newBuilder()
        .followRedirects(followRedirects)
        .followSslRedirects(followSslRedirects)
        .retryOnConnectionFailure(retryOnConnectionFailure)
        .connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
        .writeTimeout(writeTimeout.toLong(), TimeUnit.SECONDS)
        .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
        .cache(null)
        .build()
         */
      },
      wrongOptionType = { exceptionMessage ->
        promise.reject(IllegalArgumentException(exceptionMessage))
      }
    )


  }

  // Customize the notification channel as you wish. This is only for a bare minimum example
  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= 26) {
      val channel = NotificationChannel(
              notificationChannelID,
              "Background Upload Channel",
              NotificationManager.IMPORTANCE_LOW
      )
      val manager = reactApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  override fun onHostResume() {
  }

  override fun onHostPause() {
  }

  override fun onHostDestroy() {
  }
}
