package com.sogo.golf.msl.domain.model

sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val error: NetworkError) : NetworkResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : NetworkResult<T>()
}

sealed class NetworkError {
    object NoConnection : NetworkError()
    object Timeout : NetworkError()
    object ServerError : NetworkError()
    data class HttpError(val code: Int, val message: String, val isRefreshFailure: Boolean = false) : NetworkError()
    data class Unknown(val message: String) : NetworkError()

    fun toUserMessage(): String = when (this) {
        NoConnection -> "No internet connection. Please check your network and try again."
        Timeout -> "Request timed out. Please try again."
        ServerError -> "Server error. Please try again later."
        is HttpError -> when (code) {
            401 -> if (isRefreshFailure) "Session expired. Please log in again." else "Authentication failed. Please try again."
            403 -> "Access denied. You don't have permission to access this resource."
            404 -> "Resource not found."
            500 -> "Server error. Please try again later."
            else -> "Network error: $message"
        }
        is Unknown -> message.ifEmpty { "Something went wrong. Please try again." }
    }
}
