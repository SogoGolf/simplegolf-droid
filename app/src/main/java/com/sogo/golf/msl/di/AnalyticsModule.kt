package com.sogo.golf.msl.di

import android.content.Context
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.sogo.golf.msl.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {
    
    @Provides
    @Singleton
    fun provideAmplitude(@ApplicationContext context: Context): Amplitude {
        return Amplitude(
            Configuration(
                apiKey = BuildConfig.AMPLITUDE_ANALYTICS,
                context = context
            )
        )
    }
}