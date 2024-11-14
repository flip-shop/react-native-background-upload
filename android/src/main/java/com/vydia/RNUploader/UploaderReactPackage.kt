package com.vydia.RNUploader

import com.facebook.react.TurboReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.vydia.RNUploader.files.FileInfoProvider
import com.vydia.RNUploader.files.FileInfoProviderImpl
import com.vydia.RNUploader.networking.httpClient.HttpClientOptionsProvider
import com.vydia.RNUploader.networking.httpClient.HttpClientOptionsProviderImpl
import com.vydia.RNUploader.networking.request.options.UploadRequestOptionsProvider
import com.vydia.RNUploader.networking.request.options.UploadRequestOptionsProviderImpl
import com.vydia.RNUploader.notifications.config.NotificationsConfigProvider
import com.vydia.RNUploader.notifications.config.NotificationsConfigProviderImpl
import com.vydia.RNUploader.notifications.manager.NotificationChannelManager
import com.vydia.RNUploader.notifications.manager.NotificationChannelManagerImpl
import com.vydia.RNUploader.worker.UploadWorkerManager
import com.vydia.RNUploader.worker.UploadWorkerManagerImpl
import org.koin.java.KoinJavaComponent.inject

/**
 * Created by stephen on 12/8/16.
 */
class UploaderReactPackage : TurboReactPackage() {

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

    private val uploadWorkerManager: UploadWorkerManager
            by inject(UploadWorkerManagerImpl::class.java)

    override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
    return if (name == UploaderModule.NAME) {
      UploaderModule(
                reactContext = reactContext,
                fileInfoProvider = fileInfoProvider,
                httpClientOptionsProvider = httpClientOptionsProvider,
                uploadRequestOptionsProvider = uploadRequestOptionsProvider,
                notificationsConfigProvider = notificationsConfigProvider,
                notificationChannelManager = notificationChannelManager,
                uploadWorkerManager = uploadWorkerManager
            )
    } else {
      null
    }
  }

  override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
    return ReactModuleInfoProvider {
      val moduleInfos: MutableMap<String, ReactModuleInfo> = HashMap()
      val isTurboModule: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
      moduleInfos[UploaderModule.NAME] = ReactModuleInfo(
        UploaderModule.NAME,
        UploaderModule.NAME,
        false,  // canOverrideExistingModule
        false,  // needsEagerInit
        true,  // hasConstants
        false,  // isCxxModule
        isTurboModule // isTurboModule
      )
      moduleInfos
    }
  }
}