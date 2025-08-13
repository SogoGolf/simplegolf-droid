package com.sogo.golf.msl.domain.usecase.messaging

import com.sogo.golf.msl.data.messaging.FcmTokenManager
import javax.inject.Inject

class SubscribeToTopicUseCase @Inject constructor(
    private val fcmTokenManager: FcmTokenManager
) {
    operator fun invoke(topic: String) {
        fcmTokenManager.subscribeToTopic(topic)
    }
}