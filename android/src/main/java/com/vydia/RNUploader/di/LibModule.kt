package com.vydia.RNUploader.di

import com.vydia.RNUploader.event_publisher.EventsPublisherImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val libModule = module {
    single { EventsPublisherImpl(androidContext()) }
}