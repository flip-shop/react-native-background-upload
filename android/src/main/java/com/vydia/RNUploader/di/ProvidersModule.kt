package com.vydia.RNUploader.di

import com.vydia.RNUploader.files.FileInfoProviderImpl
import com.vydia.RNUploader.files.helpers.FilesHelperImpl
import com.vydia.RNUploader.files.helpers.MimeTypeHelperImpl
import com.vydia.RNUploader.networking.httpClient.HttpClientOptionsProviderImpl
import com.vydia.RNUploader.networking.request.options.UploadRequestOptionsProviderImpl
import com.vydia.RNUploader.notifications.config.NotificationsConfigProviderImpl
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
