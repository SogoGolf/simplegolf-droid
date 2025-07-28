// app/src/main/java/com/sogo/golf/msl/di/AppUpdateModule.kt
package com.sogo.golf.msl.di

import android.content.Context
import com.sogo.golf.msl.app.update.AppUpdateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppUpdateModule {

    @Provides
    @Singleton
    fun provideAppUpdateManager(
        @ApplicationContext context: Context
    ): AppUpdateManager = AppUpdateManager(context)
}