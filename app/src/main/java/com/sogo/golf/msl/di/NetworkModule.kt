package com.sogo.golf.msl.di

import android.content.Context
import com.sogo.golf.msl.BuildConfig
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.network.api.GolfApiService
import com.sogo.golf.msl.data.network.api.SogoApiService
import com.sogo.golf.msl.data.network.api.MpsAuthApiService
import com.sogo.golf.msl.data.network.interceptors.GolfApiAuthInterceptor
import com.sogo.golf.msl.data.network.interceptors.SogoApiAuthInterceptor
import com.sogo.golf.msl.data.network.interceptors.MpsAuthInterceptor
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
annotation class GolfApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SogoApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MpsAuthApi

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
    fun provideBaseOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }


//    --> GET https://golf-api.micropower.com.au/api/v2/2240662/game
//    2025-07-27 16:59:33.412 18438-20594 okhttp.OkHttpClient     com.sogo.golf.msl                    I  Authorization: Basic Sogo Sports Pty Ltd:lpx10H1j73wdBr6gCQlRafswohZFvnpghrajwt/MSSQGNREAQktNbWl/zucoKPPPKKXZpHV+FF2p23JGUAybtv+RPD16Zj6+bw+OrUU1BRrDir49vZsJzTw2mpiS7EeyZUFTrbnA+wx4m8Jek7gDAhlqpTUX7Z13A1MfjnpPDbQ=
//    2025-07-27 16:59:33.412 18438-20594 okhttp.OkHttpClient     com.sogo.golf.msl                    I  X-Member-Token: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzeXN0ZW0iOiJzb2dvc3BvcnRzcHR5bHRkIiwibmFtZWlkIjoiMDAxMzQiLCJjbHViaWQiOiI2NzAyMjkiLCJtZW1iZXJpZCI6IjEzOTQ2MzUiLCJmZWF0dXJlbW9kdWxlIjpbImJ1eWluZ2NsdWIiLCJldmVudCIsImdtcyIsIm1lbWJlcnNoaXAiLCJuZ2FnZSIsInBvcyIsInByb3BsYW5uZXIiXSwiYXV0aG1ldGhvZCI6InJlc291cmNlb3duZXIiLCJqdGkiOiJmZDc2M2Y2ZC1mN2IyLTQ3ZjAtYjY2NC1mZDczYjcwYmFjNDUiLCJuYmYiOjE3NTM1OTYwNTgsImV4cCI6MTc1MzY2ODA1OCwiaWF0IjoxNzUzNTk2MDU4LCJpc3MiOiJNUCIsImF1ZCI6Imh0dHBzOi8vbXBzYXBpLm1pY3JvcG93ZXIuY29tLmF1In0.PfCdbvnkaDNHS493v7rljddJHNeAlVsWZZlQT--r24E
//    2025-07-27 16:59:33.412 18438-20594 okhttp.OkHttpClient     com.sogo.golf.msl                    I  --> END GET
//    2025-07-27 16:59:33.494 18438-20594 okhttp.OkHttpClient     com.sogo.golf.msl                    I  <-- 401 https://golf-api.micropower.com.au/api/v2/2240662/game (81ms)

    // Golf API - golf-api.micropower.com.au
    @Provides
    @Singleton
    @GolfApi
    fun provideGolfApiRetrofit(
        loggingInterceptor: HttpLoggingInterceptor,
        golfApiAuthInterceptor: GolfApiAuthInterceptor
    ): Retrofit {
        val golfApiClient = OkHttpClient.Builder()
            .addInterceptor(golfApiAuthInterceptor)  // Add auth headers FIRST
            .addInterceptor(loggingInterceptor)      // Then log the request WITH headers
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://${BuildConfig.MSL_GOLF_URL}/")
            .client(golfApiClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Sogo API - sogo-api.azure-api.net
    @Provides
    @Singleton
    @SogoApi
    fun provideSogoApiRetrofit(
        baseOkHttpClient: OkHttpClient,
        sogoApiAuthInterceptor: SogoApiAuthInterceptor
    ): Retrofit {
        val sogoApiClient = baseOkHttpClient.newBuilder()
            .addInterceptor(sogoApiAuthInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://${BuildConfig.SOGO_MSL_AUTH_URL}/")
            .client(sogoApiClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // MPS Auth API - mpsapi.micropower.com.au
    @Provides
    @Singleton
    @MpsAuthApi
    fun provideMpsAuthApiRetrofit(
        baseOkHttpClient: OkHttpClient,
        mpsAuthInterceptor: MpsAuthInterceptor
    ): Retrofit {
        val mpsAuthApiClient = baseOkHttpClient.newBuilder()
            .addInterceptor(mpsAuthInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://${BuildConfig.MSL_BASE_URL_AUTH}/")
            .client(mpsAuthApiClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API Service Providers
    @Provides
    @Singleton
    fun provideGolfApiService(@GolfApi retrofit: Retrofit): GolfApiService {
        return retrofit.create(GolfApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSogoApiService(@SogoApi retrofit: Retrofit): SogoApiService {
        return retrofit.create(SogoApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMpsAuthApiService(@MpsAuthApi retrofit: Retrofit): MpsAuthApiService {
        return retrofit.create(MpsAuthApiService::class.java)
    }
}