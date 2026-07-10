package com.sogo.golf.msl.shared.utils

import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.msl.MslGame
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

object TimeFormatUtils {

    /**
     * Pace-of-play clock: H:MM:SS once over an hour, else M:SS
     * (includeSeconds = false gives H:MM). Negatives clamp to 0.
     * Shared by the Play screen's pace popover and the Review screen's
     * submit-success "Round Time" so both format identically.
     */
    fun formatPaceClock(seconds: Int, includeSeconds: Boolean): String {
        val total = maxOf(0, seconds)
        val hours = total / 3600
        val minutes = (total % 3600) / 60
        val secs = total % 60
        return if (includeSeconds) {
            if (hours > 0) "%d:%02d:%02d".format(hours, minutes, secs)
            else "%d:%02d".format(minutes, secs)
        } else {
            "%d:%02d".format(hours, minutes)
        }
    }

    /**
     * Human-friendly duration for the submit-success dialog's Round Time:
     * "4 hr 53 min" once into the hours (seconds dropped — noise at that
     * precision), "7 min 53 sec" under an hour, zero secondary units omitted
     * ("4 hr", "7 min"). The pace popover keeps the stopwatch-style
     * formatPaceClock above. Must produce IDENTICAL strings to iOS
     * (PaceStatus.friendlyDuration).
     */
    fun formatFriendlyDuration(seconds: Int): String {
        val total = maxOf(0, seconds)
        val hours = total / 3600
        val minutes = (total % 3600) / 60
        val secs = total % 60
        return when {
            hours > 0 -> if (minutes > 0) "$hours hr $minutes min" else "$hours hr"
            minutes > 0 -> if (secs > 0) "$minutes min $secs sec" else "$minutes min"
            else -> "$secs sec"
        }
    }

    /**
     * The pace clock's start anchor. Runs from the BOOKED tee time — a late
     * start counts against pace; the "Let's Play" tap happens in the pro shop
     * before the tee time and must not pull the clock earlier. Falls back to
     * the actual round start only when there is no booked tee time.
     */
    fun resolvePaceStartMillis(game: MslGame?, round: Round?): Long? {
        val actualStartMillis = round?.startTime?.toEpochMillis()
        val bookingTime = game?.bookingTime
        val teeMillis = if (bookingTime != null) {
            val roundDate = round?.roundDate?.toLocalDate() ?: LocalDate.now()
            LocalDateTime.of(roundDate, bookingTime.toLocalTime()).toEpochMillis()
        } else {
            null
        }
        return teeMillis ?: actualStartMillis
    }

    private fun LocalDateTime.toEpochMillis(): Long =
        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    
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
