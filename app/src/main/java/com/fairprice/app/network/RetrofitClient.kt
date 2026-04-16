package com.fairprice.app.network

import android.content.Context
import com.fairprice.app.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for the PDS Fair Price Go backend.
 *
 * Uses 10.0.2.2 for Android emulator → host machine mapping.
 * Change BASE_URL for physical device testing.
 */
object RetrofitClient {

    // For emulator: 10.0.2.2 maps to host machine localhost
    // For physical device: use your machine's LAN IP
    private const val BASE_URL = "http://10.0.2.2:8080/"
    
    private var sessionManager: SessionManager? = null

    fun initialize(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager.getInstance(context)
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
        
        sessionManager?.getAccessToken()?.let { token ->
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        chain.proceed(requestBuilder.build())
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
