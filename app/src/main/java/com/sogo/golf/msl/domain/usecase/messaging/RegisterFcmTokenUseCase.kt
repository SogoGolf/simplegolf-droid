package com.sogo.golf.msl.domain.usecase.messaging

import com.sogo.golf.msl.data.messaging.FcmTokenManager
import javax.inject.Inject

class RegisterFcmTokenUseCase @Inject constructor(
    private val fcmTokenManager: FcmTokenManager
) {
    suspend operator fun invoke(): String? {
        return fcmTokenManager.getCurrentToken()
    }
}