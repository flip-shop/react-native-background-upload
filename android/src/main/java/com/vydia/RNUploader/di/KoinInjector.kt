package com.vydia.RNUploader.di

import org.koin.core.module.Module

/** list of all DI modules used in this library **/
val backgroundUploadModules: List<Module> = listOf(
    libModule,
    networkModule,
    providersModule,
    notificationModule
)