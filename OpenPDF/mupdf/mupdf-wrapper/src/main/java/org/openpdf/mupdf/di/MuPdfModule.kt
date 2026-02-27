package org.openpdf.mupdf.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MuPdfModule {
    // MuPdfEngine and PageBitmapCache are constructor-injected singletons.
    // MuPdfDispatcher is provided by CoroutineModule in core-common.
}
