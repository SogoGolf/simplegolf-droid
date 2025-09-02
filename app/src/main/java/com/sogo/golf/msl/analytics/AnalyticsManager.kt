package com.sogo.golf.msl.analytics

import android.util.Log
import com.amplitude.android.Amplitude
import com.amplitude.android.events.Identify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    private val amplitude: Amplitude
) {
    
    companion object {
        private const val TAG = "AnalyticsManager"
        
        // Event names
        const val EVENT_LOGIN_INITIATED = "login_initiated"
        const val EVENT_LOGIN_FAILED = "login_failed"
        const val EVENT_LOGIN_COMPLETED = "login_completed"
        const val EVENT_USER_LOGOUT = "logout"
        const val EVENT_CONFIRM_GOLFER_DATA_DISPLAYED = "confirm_golfer_data_displayed"
        const val EVENT_CONFIRM_GOLFER_DATA_SUCCESS = "confirm_golfer_data_success"
        const val EVENT_COMPETITIONS_VIEWED = "competitions_viewed"
        const val EVENT_OPEN_SIDE_MENU = "open_side_menu"
        const val EVENT_INCLUDE_ROUND_TOGGLED = "include_round_toggled"
        const val EVENT_PLAYING_PARTNER_SCREEN_VIEWED = "playing_partner_screen_viewed"
        const val EVENT_PLAYING_PARTNER_SELECTED = "playing_partner_selected"
        const val EVENT_PLAYING_PARTNER_DESELECTED = "playing_partner_deselected"
        const val EVENT_ROUND_STARTED = "round_started"
        const val EVENT_ROUND_RESET_MARKER = "round_reset_marker"
        const val EVENT_SCORECARD_VIEWED = "scorecard_viewed"
        const val EVENT_PICKUP_TAPPED = "pickup_tapped"
        const val EVENT_SIGNATURE_CAPTURED = "signature_captured"
        const val EVENT_ROUND_SUBMITTED = "round_submitted"
        const val EVENT_DELETE_MARKER_API_ERROR = "delete_marker_api_error"
    }
    
    fun setUserId(userId: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                amplitude.setUserId(userId)
                Log.d(TAG, "Set Amplitude user ID: $userId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set user ID", e)
            }
        }
    }
    
    fun identifyUser(properties: Map<String, Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val identify = Identify()
                properties.forEach { (key, value) ->
                    when (value) {
                        is String -> identify.set(key, value)
                        is Number -> identify.set(key, value.toLong())
                        is Boolean -> identify.set(key, value)
                        else -> identify.set(key, value.toString())
                    }
                }
                amplitude.identify(identify)
                Log.d(TAG, "Identified user with ${properties.size} properties")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to identify user", e)
            }
        }
    }
    
    fun trackLogout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                amplitude.track(EVENT_USER_LOGOUT)
                amplitude.setUserId(null) // Clear user ID on logout
                Log.d(TAG, "Tracked logout event")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to track logout event", e)
            }
        }
    }
    
    fun trackEvent(eventName: String, properties: Map<String, Any>? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (properties != null) {
                    amplitude.track(eventName, properties)
                } else {
                    amplitude.track(eventName)
                }
                Log.d(TAG, "Tracked event: $eventName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to track event: $eventName", e)
            }
        }
    }
}
