package com.vydia.RNUploader.di

import org.koin.core.module.Module

/** list of all DI modules used in this library **/
val koinInjector: List<Module> = listOf(networkModule, providersModule)