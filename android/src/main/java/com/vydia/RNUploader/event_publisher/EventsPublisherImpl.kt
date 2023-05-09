package com.vydia.RNUploader.event_publisher

import android.content.Context
import android.util.Log.d
import com.facebook.react.ReactApplication
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.vydia.RNUploader.helpers.emitEventErrorJsModuleNull

private const val TAG = "EventsPublisher"
class EventsPublisherImpl(
    private val context: Context
): EventsPublisher {

    override fun sendEvent(event: UploadEvents, params: WritableMap?) {
        (context as? ReactApplication)?.reactNativeHost?.reactInstanceManager?.currentReactContext?.let { reactContext ->
            val jsModule = reactContext.getJSModule(
                DeviceEventManagerModule.RCTDeviceEventEmitter::class.java
            )

            if(jsModule == null) {
                d(TAG, emitEventErrorJsModuleNull)
                return
            }

            jsModule.emit(event.name, params)
        }
    }

}