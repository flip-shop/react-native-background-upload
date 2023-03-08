package com.vydia.RNUploader.di

import com.vydia.RNUploader.notifications.data.NotificationActionProviderImpl
import com.vydia.RNUploader.notifications.manager.NotificationChannelManagerImpl
import com.vydia.RNUploader.notifications.manager.NotificationCreatorImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val notificationModule = module {
    single { NotificationChannelManagerImpl(androidContext()) }
    single { NotificationCreatorImpl(androidContext()) }
    single { NotificationActionProviderImpl(androidContext()) }
}