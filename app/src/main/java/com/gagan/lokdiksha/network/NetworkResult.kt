package com.gagan.lokdiksha.network

import com.google.gson.Gson
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Sealed class representing the result of a network operation.
 * Used across the app for consistent Loading / Success / Error state handling.
 */
sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
    ) : NetworkResult<Nothing>()
}

/**
 * Wraps a Retrofit suspend call with comprehensive error handling.
 *
 * Catches:
 * - No internet (UnknownHostException)
 * - Timeout (SocketTimeoutException)
 * - Connection refused (ConnectException)
 * - HTTP 401 Unauthorized
 * - HTTP 403 Forbidden (geofence / rate-limit)
 * - HTTP 500+ Server errors
 * - All other exceptions
 *
 * @return [NetworkResult.Success] with parsed body, or [NetworkResult.Error] with user-friendly message.
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error("Empty response from server", response.code())
            }
        } else {
            // Parse structured error body if available
            val errorBody = response.errorBody()?.string()
            val apiError = try {
                Gson().fromJson(errorBody, ApiErrorResponse::class.java)
            } catch (_: Exception) {
                null
            }

            val userMessage = when (response.code()) {
                401 -> "Session expired. Please login again."
                403 -> apiError?.message ?: "Access denied."
                404 -> apiError?.message ?: "Resource not found."
                429 -> apiError?.message ?: "Too many requests. Please wait and try again."
                in 500..599 -> "Server error. Please try again later."
                else -> apiError?.message ?: "Something went wrong (${response.code()})."
            }
            NetworkResult.Error(userMessage, response.code())
        }
    } catch (e: UnknownHostException) {
        NetworkResult.Error("Check your internet connection.")
    } catch (e: SocketTimeoutException) {
        NetworkResult.Error("Server is not responding. Try again later.")
    } catch (e: ConnectException) {
        NetworkResult.Error("Unable to connect to server. Please check if the server is running.")
    } catch (e: Exception) {
        NetworkResult.Error(e.localizedMessage ?: "An unexpected error occurred.")
    }
}
