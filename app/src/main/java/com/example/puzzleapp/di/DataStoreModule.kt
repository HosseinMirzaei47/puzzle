package com.example.puzzleapp.di

import android.content.Context
import com.example.puzzleapp.utils.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object DataStoreModule {

    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context) = Settings(context)

}