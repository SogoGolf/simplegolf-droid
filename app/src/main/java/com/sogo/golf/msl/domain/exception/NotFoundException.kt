package com.sogo.golf.msl.domain.exception

/**
 * Exception thrown when a resource is not found (HTTP 404).
 * This is often an expected scenario (e.g., new user doesn't have a SOGO account yet)
 * and should not be logged to Sentry as an error.
 */
class NotFoundException(message: String) : Exception(message)
