// app/src/main/java/com/sogo/golf/msl/shared/utils/DateUtils.kt
package com.sogo.golf.msl.shared.utils

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

object DateUtils {
    private const val DATE_FORMAT = "yyyy-MM-dd"
    private val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)

    /**
     * Get today's date as a string in format "yyyy-MM-dd"
     * Uses ThreeTen library for API 24+ compatibility
     */
    fun getTodayDateString(): String {
        return LocalDate.now().format(formatter)
    }

    /**
     * Check if two date strings represent the same date
     * @param date1 Date string in format "yyyy-MM-dd"
     * @param date2 Date string in format "yyyy-MM-dd"
     * @return true if dates are the same, false otherwise
     */
    fun isSameDate(date1: String?, date2: String?): Boolean {
        if (date1 == null || date2 == null) return false

        return try {
            val localDate1 = LocalDate.parse(date1, formatter)
            val localDate2 = LocalDate.parse(date2, formatter)
            localDate1.isEqual(localDate2)
        } catch (e: Exception) {
            android.util.Log.w("DateUtils", "Error parsing dates: $date1, $date2", e)
            false
        }
    }

    /**
     * Check if a date string represents today
     */
    fun isToday(dateString: String?): Boolean {
        return isSameDate(dateString, getTodayDateString())
    }

    /**
     * Get a human-readable representation of the date
     * @param dateString Date string in format "yyyy-MM-dd"
     * @return Formatted date like "Jul 28, 2025"
     */
    fun formatDateForDisplay(dateString: String?): String {
        if (dateString == null) return "Unknown date"

        return try {
            val localDate = LocalDate.parse(dateString, formatter)
            localDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } catch (e: Exception) {
            android.util.Log.w("DateUtils", "Error formatting date: $dateString", e)
            dateString
        }
    }

    /**
     * Get the number of days between two dates
     * @param fromDate Date string in format "yyyy-MM-dd"
     * @param toDate Date string in format "yyyy-MM-dd"
     * @return Number of days between dates, or null if parsing fails
     */
    fun daysBetween(fromDate: String?, toDate: String?): Long? {
        if (fromDate == null || toDate == null) return null

        return try {
            val startDate = LocalDate.parse(fromDate, formatter)
            val endDate = LocalDate.parse(toDate, formatter)
            startDate.until(endDate).days.toLong()
        } catch (e: Exception) {
            android.util.Log.w("DateUtils", "Error calculating days between: $fromDate, $toDate", e)
            null
        }
    }

    // 🔧 DEBUG: Override today's date for testing
    private var debugDate: String? = null

    fun setDebugDate(date: String?) {
        debugDate = date
        android.util.Log.d("DateUtils", "🔧 DEBUG: Set debug date to: $date")
    }

    fun clearDebugDate() {
        debugDate = null
        android.util.Log.d("DateUtils", "🔧 DEBUG: Cleared debug date, using real date")
    }
}