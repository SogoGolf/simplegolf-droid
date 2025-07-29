package com.sogo.golf.msl.shared.utils

import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

object TimeFormatUtils {
    
    /**
     * Formats booking time from LocalDateTime to user-friendly format
     * Example: "1900-01-01T23:55:00" -> "11:55 PM"
     * Compatible with API 24+
     */
    fun formatBookingTime(bookingTime: LocalDateTime?): String {
        return try {
            if (bookingTime == null) {
                "-"
            } else {
                // Use ThreeTen BackPort compatible formatter for API 24+
                val timeFormatter = DateTimeFormatter.ofPattern("h:mma", Locale.getDefault())
                bookingTime.format(timeFormatter)
            }
        } catch (e: Exception) {
            android.util.Log.w("TimeFormatUtils", "Error formatting booking time: ${e.message}")
            // Fallback to basic string representation if formatting fails
            bookingTime?.let { 
                try {
                    // Extract hour and minute manually as fallback
                    val hour = it.hour
                    val minute = it.minute
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                    "$displayHour:${minute.toString().padStart(2, '0')} $amPm"
                } catch (fallbackException: Exception) {
                    android.util.Log.w("TimeFormatUtils", "Fallback formatting also failed: ${fallbackException.message}")
                    "-"
                }
            } ?: "-"
        }
    }
    
    /**
     * Alternative format for 24-hour time display
     * Example: "1900-01-01T23:55:00" -> "23:55"
     * Compatible with API 24+
     */
    fun formatBookingTime24Hour(bookingTime: LocalDateTime?): String {
        return try {
            if (bookingTime == null) {
                "-"
            } else {
                // Use ThreeTen BackPort compatible formatter for API 24+
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
                bookingTime.format(timeFormatter)
            }
        } catch (e: Exception) {
            android.util.Log.w("TimeFormatUtils", "Error formatting booking time: ${e.message}")
            // Fallback to manual formatting if DateTimeFormatter fails
            bookingTime?.let {
                try {
                    val hour = it.hour.toString().padStart(2, '0')
                    val minute = it.minute.toString().padStart(2, '0')
                    "$hour:$minute"
                } catch (fallbackException: Exception) {
                    android.util.Log.w("TimeFormatUtils", "Fallback formatting also failed: ${fallbackException.message}")
                    "-"
                }
            } ?: "-"
        }
    }
}
