package com.vydia.RNUploader.di

import com.vydia.RNUploader.files.FileInfoProvider
import com.vydia.RNUploader.files.FileInfoProviderImpl
import com.vydia.RNUploader.files.helpers.FilesHelperImpl
import com.vydia.RNUploader.files.helpers.MimeTypeHelperImpl
import com.vydia.RNUploader.networking.httpClient.HttpClientOptionsProvider
import com.vydia.RNUploader.networking.httpClient.HttpClientOptionsProviderImpl
import com.vydia.RNUploader.networking.request.options.UploadRequestOptionsProvider
import com.vydia.RNUploader.networking.request.options.UploadRequestOptionsProviderImpl
import com.vydia.RNUploader.notifications.NotificationsConfigProvider
import com.vydia.RNUploader.notifications.NotificationsConfigProviderImpl
import org.koin.dsl.module

val providersModule = module {

    factory {
        FileInfoProviderImpl(
            mimeTypeHelper = MimeTypeHelperImpl(),
            filesHelper = FilesHelperImpl()
        )
    }

    factory {
        HttpClientOptionsProviderImpl()
    }

    factory {
        UploadRequestOptionsProviderImpl()
    }

    factory {
        NotificationsConfigProviderImpl()
    }





}
