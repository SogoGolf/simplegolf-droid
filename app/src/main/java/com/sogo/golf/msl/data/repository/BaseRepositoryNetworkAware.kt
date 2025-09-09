package com.sogo.golf.msl.data.repository

import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.domain.model.NetworkError
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.exception.TokenRefreshException
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
            Sentry.captureException(e)
            NetworkResult.Error(NetworkError.TokenRefreshFailed)
        } catch (e: TimeoutCancellationException) {
            Sentry.captureException(e)
            NetworkResult.Error(NetworkError.Timeout)
        } catch (e: IOException) {
            Sentry.captureException(e)
            NetworkResult.Error(NetworkError.NoConnection)
        } catch (e: HttpException) {
            Sentry.captureException(e)
            NetworkResult.Error(NetworkError.ServerError)
        } catch (e: Exception) {
            Sentry.captureException(e)
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Unknown error"))
        }
    }
}
