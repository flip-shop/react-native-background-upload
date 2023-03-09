package com.vydia.RNUploader

import com.facebook.react.bridge.ReactContext
import com.vydia.RNUploader.di.koinInjector
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class KoinInitializer {
    companion object {
        private var initialized = false
        fun init(context: ReactContext) {
            if(initialized) {
                return
            }
            startKoin {
                androidContext(context)
                modules(koinInjector)
            }
            initialized = true
        }
    }
}