package com.sogo.golf.msl.features.home.presentation

import org.junit.Test
import org.junit.Assert.*

class PostcodeValidationTest {

    // Create a simple test utility to access the validation methods
    private fun isValidAustralianPostcode(postcode: String): Boolean {
        val normalized = postcode.trim()
        val postcodeRegex = "^[0-8]\\d{3}$".toRegex()
        return postcodeRegex.matches(normalized)
    }

    private fun isPostcodeValidForState(postcode: String, state: String): Boolean {
        val normalized = postcode.trim()
        if (!isValidAustralianPostcode(normalized)) return false
        
        val postcodeInt = normalized.toIntOrNull() ?: return false
        
        return when (state.uppercase()) {
            "NSW" -> postcodeInt in 1000..2599 || postcodeInt in 2619..2899 || postcodeInt in 2921..2999
            "ACT" -> postcodeInt in 2600..2618 || postcodeInt in 2900..2920
            "VIC" -> postcodeInt in 3000..3999 || postcodeInt in 8000..8999
            "QLD" -> postcodeInt in 4000..4999 || postcodeInt in 9000..9999
            "SA" -> postcodeInt in 5000..5999
            "WA" -> postcodeInt in 6000..6797 || postcodeInt in 6800..6999
            "TAS" -> postcodeInt in 7000..7999
            "NT" -> postcodeInt in 800..999
            else -> false
        }
    }

    @Test
    fun `isValidAustralianPostcode should accept valid postcodes`() {
        assertTrue(isValidAustralianPostcode("2448"))
        assertTrue(isValidAustralianPostcode("3000"))
        assertTrue(isValidAustralianPostcode("4000"))
        assertTrue(isValidAustralianPostcode("5000"))
        assertTrue(isValidAustralianPostcode("6000"))
        assertTrue(isValidAustralianPostcode("7000"))
        assertTrue(isValidAustralianPostcode("0800"))
    }

    @Test
    fun `isValidAustralianPostcode should accept valid postcodes with whitespace`() {
        assertTrue(isValidAustralianPostcode(" 2448"))
        assertTrue(isValidAustralianPostcode("2448 "))
        assertTrue(isValidAustralianPostcode(" 2448 "))
        assertTrue(isValidAustralianPostcode("2448\n"))
        assertTrue(isValidAustralianPostcode("\t2448"))
    }

    @Test
    fun `isValidAustralianPostcode should reject invalid postcodes`() {
        assertFalse(isValidAustralianPostcode("244"))
        assertFalse(isValidAustralianPostcode("24489"))
        assertFalse(isValidAustralianPostcode("abcd"))
        assertFalse(isValidAustralianPostcode("9999"))
        assertFalse(isValidAustralianPostcode(""))
        assertFalse(isValidAustralianPostcode("   "))
    }

    @Test
    fun `isPostcodeValidForState should accept valid NSW postcodes`() {
        assertTrue(isPostcodeValidForState("2448", "NSW"))
        assertTrue(isPostcodeValidForState("1000", "NSW"))
        assertTrue(isPostcodeValidForState("2599", "NSW"))
        assertTrue(isPostcodeValidForState("2619", "NSW"))
        assertTrue(isPostcodeValidForState("2899", "NSW"))
        assertTrue(isPostcodeValidForState("2921", "NSW"))
        assertTrue(isPostcodeValidForState("2999", "NSW"))
    }

    @Test
    fun `isPostcodeValidForState should accept valid NSW postcodes with whitespace`() {
        assertTrue(isPostcodeValidForState(" 2448", "NSW"))
        assertTrue(isPostcodeValidForState("2448 ", "NSW"))
        assertTrue(isPostcodeValidForState(" 2448 ", "NSW"))
        assertTrue(isPostcodeValidForState("2448\n", "NSW"))
    }

    @Test
    fun `isPostcodeValidForState should reject invalid NSW postcodes`() {
        assertFalse(isPostcodeValidForState("2600", "NSW")) // ACT range
        assertFalse(isPostcodeValidForState("2900", "NSW")) // ACT range
        assertFalse(isPostcodeValidForState("3000", "NSW")) // VIC range
        assertFalse(isPostcodeValidForState("4000", "NSW")) // QLD range
    }

    @Test
    fun `isPostcodeValidForState should work for other states`() {
        assertTrue(isPostcodeValidForState("2600", "ACT"))
        assertTrue(isPostcodeValidForState("3000", "VIC"))
        assertTrue(isPostcodeValidForState("4000", "QLD"))
        assertTrue(isPostcodeValidForState("5000", "SA"))
        assertTrue(isPostcodeValidForState("6000", "WA"))
        assertTrue(isPostcodeValidForState("7000", "TAS"))
        assertTrue(isPostcodeValidForState("0800", "NT"))
    }

    @Test
    fun `isPostcodeValidForState should reject postcode-state mismatches`() {
        assertFalse(isPostcodeValidForState("2448", "VIC"))
        assertFalse(isPostcodeValidForState("3000", "NSW"))
        assertFalse(isPostcodeValidForState("4000", "SA"))
    }

    @Test
    fun `isPostcodeValidForState should handle case insensitive states`() {
        assertTrue(isPostcodeValidForState("2448", "nsw"))
        assertTrue(isPostcodeValidForState("2448", "Nsw"))
        assertTrue(isPostcodeValidForState("3000", "vic"))
        assertTrue(isPostcodeValidForState("3000", "VIC"))
    }
}
