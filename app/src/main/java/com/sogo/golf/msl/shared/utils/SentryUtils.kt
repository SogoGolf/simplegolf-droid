package com.sogo.golf.msl.shared.utils

import io.sentry.Sentry
import io.sentry.SentryLogLevel
import io.sentry.Sentry.logger

object SentryUtils {
    
    fun sentryLog(level: SentryLogLevel, message: String, properties: Map<String, Any?> = emptyMap()) {
        if (properties.isNotEmpty()) {
            properties.forEach { (key, value) ->
                Sentry.setTag(key, value?.toString() ?: "null")
            }
        }
        logger().log(level, message)
    }
    
    fun sentryLog(level: SentryLogLevel, message: String) {
        logger().log(level, message)
    }
    
    fun sentryLogException(exception: Throwable, message: String? = null, properties: Map<String, Any?> = emptyMap()) {
        if (properties.isNotEmpty()) {
            properties.forEach { (key, value) ->
                Sentry.setTag(key, value?.toString() ?: "null")
            }
        }
        if (message != null) {
            Sentry.captureMessage(message)
        }
        Sentry.captureException(exception)
    }
    
    fun sentryLogMessage(message: String, properties: Map<String, Any?> = emptyMap()) {
        if (properties.isNotEmpty()) {
            properties.forEach { (key, value) ->
                Sentry.setTag(key, value?.toString() ?: "null")
            }
        }
        Sentry.captureMessage(message)
    }
}
