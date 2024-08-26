package com.raafat.revise.di

import android.app.Application
import com.raafat.revise.persistence.AyaDao
import com.raafat.revise.main.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    @Provides
    @ViewModelScoped
    fun provideMainRepository(
        application: Application,
        ayaDao: AyaDao
    ): MainRepository {
        return MainRepository(application, ayaDao)
    }
}