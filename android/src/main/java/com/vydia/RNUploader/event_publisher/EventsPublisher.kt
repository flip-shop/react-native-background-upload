package com.vydia.RNUploader.event_publisher

import com.facebook.react.bridge.WritableMap

interface EventsPublisher {
    fun sendEvent(event: UploadEvents, params: WritableMap?)
}