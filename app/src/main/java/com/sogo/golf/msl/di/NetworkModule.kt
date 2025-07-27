package com.sogo.golf.msl.di

import android.content.Context
import com.sogo.golf.msl.BuildConfig
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.network.api.MslApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MslGolfApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MslSogoApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MslMpsApi

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkChecker(
        @ApplicationContext context: Context
    ): NetworkChecker = NetworkChecker(context)

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @MslGolfApi
    fun provideMslGolfRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://${BuildConfig.MSL_GOLF_URL}/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @MslSogoApi
    fun provideMslSogoRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://${BuildConfig.SOGO_MSL_AUTH_URL}/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @MslMpsApi
    fun provideMslMpsRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://${BuildConfig.MSL_BASE_URL_AUTH}/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @MslGolfApi
    fun provideMslGolfApiService(@MslGolfApi retrofit: Retrofit): MslApiService {
        return retrofit.create(MslApiService::class.java)
    }

    @Provides
    @Singleton
    @MslSogoApi
    fun provideMslSogoApiService(@MslSogoApi retrofit: Retrofit): MslApiService {
        return retrofit.create(MslApiService::class.java)
    }

    @Provides
    @Singleton
    @MslMpsApi
    fun provideMslMpsApiService(@MslMpsApi retrofit: Retrofit): MslApiService {
        return retrofit.create(MslApiService::class.java)
    }
}