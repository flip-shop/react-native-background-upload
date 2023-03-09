package com.vydia.RNUploader

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.vydia.RNUploader.di.koinInjector
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
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject

/**
 * Created by stephen on 12/8/16.
 */
class UploaderReactPackage : ReactPackage {

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

    // Deprecated in RN 0.47, @todo remove after < 0.47 support remove
    fun createJSModules(): List<Class<out JavaScriptModule?>> {
        return emptyList()
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        KoinInitializer.init(reactContext)

        val modules: MutableList<NativeModule> = ArrayList()
        modules.add(
            UploaderModule(
                reactContext = reactContext,
                fileInfoProvider = fileInfoProvider,
                httpClientOptionsProvider = httpClientOptionsProvider,
                uploadRequestOptionsProvider = uploadRequestOptionsProvider,
                notificationsConfigProvider = notificationsConfigProvider,
                notificationChannelManager = notificationChannelManager,
                uploadWorkerManager = uploadWorkerManager
            )
        )
        return modules
    }
}