package com.vydia.RNUploader.di

import com.vydia.RNUploader.event_publisher.EventsPublisherImpl
import com.vydia.RNUploader.worker.UploadWorkerInitializerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val libModule = module {
    single { EventsPublisherImpl(androidContext()) }
    single { UploadWorkerInitializerImpl(androidContext()) }
}