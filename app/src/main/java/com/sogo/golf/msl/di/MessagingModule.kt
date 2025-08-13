package com.sogo.golf.msl.di

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.sogo.golf.msl.data.messaging.FcmTokenManager
import com.sogo.golf.msl.data.messaging.NotificationManagerWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MessagingModule {

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideFcmTokenManager(
        firebaseMessaging: FirebaseMessaging
    ): FcmTokenManager = FcmTokenManager(firebaseMessaging)

    @Provides
    @Singleton
    fun provideNotificationManagerWrapper(
        @ApplicationContext context: Context
    ): NotificationManagerWrapper = NotificationManagerWrapper(context)
}