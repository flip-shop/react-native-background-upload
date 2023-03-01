package com.vydia.RNUploader.di

import com.vydia.RNUploader.networking.UploadRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

val networkModule = module {


    /**
     * Enable http logs
     */

    single<Interceptor> {
        HttpLoggingInterceptor()
            .apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    /**
     * Define [OkHttpClient] object as singleton
     * set logging interceptor as an interceptor
     */

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<Interceptor>())
            .build()
    }


    /**
     *
     */

    factory {
        UploadRepository()
    }


}