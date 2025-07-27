package com.sogo.golf.msl.data.repository

import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.domain.model.NetworkError
import com.sogo.golf.msl.domain.model.NetworkResult
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

abstract class BaseRepository(
    private val networkChecker: NetworkChecker
) {

    protected suspend fun <T> safeNetworkCall(
        timeoutMs: Long = 10000L,
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

        } catch (e: TimeoutCancellationException) {
            NetworkResult.Error(NetworkError.Timeout)
        } catch (e: IOException) {
            NetworkResult.Error(NetworkError.NoConnection)
        } catch (e: HttpException) {
            NetworkResult.Error(NetworkError.ServerError)
        } catch (e: Exception) {
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Unknown error"))
        }
    }
}