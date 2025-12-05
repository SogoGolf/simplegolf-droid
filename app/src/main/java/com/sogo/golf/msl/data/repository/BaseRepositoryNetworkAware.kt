package com.sogo.golf.msl.data.repository

import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.domain.model.NetworkError
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.exception.TokenRefreshException
import com.sogo.golf.msl.domain.exception.NotFoundException
import com.sogo.golf.msl.domain.exception.PartnerReservedException
import io.sentry.Sentry
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.CancellationException

abstract class BaseRepository(
    private val networkChecker: NetworkChecker
) {

    protected suspend fun <T> safeNetworkCall(
        timeoutMs: Long = 30000L,
        apiCall: suspend () -> T
    ): NetworkResult<T> {
        return try {
            // Check network availability before making the call
            if (!networkChecker.isNetworkAvailable()) {
                return NetworkResult.Error(NetworkError.NoConnection)
            }

            // Make the network call with timeout
            val result = withTimeout(timeoutMs) {
                apiCall()
            }

            NetworkResult.Success(result)

        } catch (e: CancellationException) {
            // Don't treat coroutine cancellation as an error - just re-throw
            // This allows normal cancellation to propagate up the coroutine hierarchy
            throw e
        } catch (e: TokenRefreshException) {
            // Log token refresh issues - these are worth investigating
            Sentry.captureException(e)
            NetworkResult.Error(NetworkError.TokenRefreshFailed)
        } catch (e: TimeoutCancellationException) {
            // Don't log to Sentry - timeouts are usually network/server load issues, not app bugs
            NetworkResult.Error(NetworkError.Timeout)
        } catch (e: IOException) {
            // Don't log to Sentry - includes UnknownHostException, SocketTimeoutException, etc.
            // These are user network connectivity issues, not app bugs
            NetworkResult.Error(NetworkError.NoConnection)
        } catch (e: NotFoundException) {
            // Don't log to Sentry - 404 is often expected (e.g., new user without SOGO account)
            NetworkResult.Error(NetworkError.NotFound)
        } catch (e: PartnerReservedException) {
            // Don't log to Sentry - partner already reserved is an expected scenario
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Partner already reserved"))
        } catch (e: HttpException) {
            // Don't log to Sentry - HTTP errors (4xx, 5xx) are server-side issues
            // Only log if it's an unexpected status code that indicates an app bug
            if (e.code() in 500..599) {
                // Server errors might indicate backend issues worth tracking
                Sentry.captureException(e)
            }
            NetworkResult.Error(NetworkError.ServerError)
        } catch (e: Exception) {
            // Log unexpected exceptions - these could be actual bugs
            Sentry.captureException(e)
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Unknown error"))
        }
    }
}
