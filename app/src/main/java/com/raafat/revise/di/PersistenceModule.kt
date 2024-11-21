package com.raafat.revise.di

import android.app.Application
import androidx.room.Room
import com.raafat.revise.R
import com.raafat.revise.persistence.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): AppDatabase {
        return Room
            .databaseBuilder(
                application,
                AppDatabase::class.java,
                application.getString(R.string.revise_database)
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAyaDao(appDatabase: AppDatabase) = appDatabase.ayaDao()

}