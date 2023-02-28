package com.vydia.RNUploader.di

import com.vydia.RNUploader.baseUrl
import com.vydia.RNUploader.networking.UploadRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit

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
     * Create a singleton instance of [Retrofit]
     * set base url because it's necessary
     * but it will be override every time when the request will be calling
     */

    single {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(get<OkHttpClient>())
            .build()
    }


    /**
     *
     */

    factory {
        UploadRepository()
    }


}