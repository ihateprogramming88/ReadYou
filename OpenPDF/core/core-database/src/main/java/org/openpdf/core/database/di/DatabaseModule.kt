package org.openpdf.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.openpdf.core.database.PdfDatabase
import org.openpdf.core.database.dao.BookmarkDao
import org.openpdf.core.database.dao.RecentDocumentDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PdfDatabase =
        Room.databaseBuilder(
            context,
            PdfDatabase::class.java,
            "openpdf.db",
        ).build()

    @Provides
    fun provideRecentDocumentDao(database: PdfDatabase): RecentDocumentDao =
        database.recentDocumentDao()

    @Provides
    fun provideBookmarkDao(database: PdfDatabase): BookmarkDao =
        database.bookmarkDao()
}
