package com.sogo.golf.msl.domain.exception

/**
 * Exception thrown when a playing partner is already reserved by another user.
 * This is an expected scenario and should not be logged to Sentry as an error.
 */
class PartnerReservedException(message: String) : Exception(message)
