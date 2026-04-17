package com.fairprice.app.network

import android.content.Context
import android.util.Log
import com.fairprice.app.BuildConfig
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
 * BASE_URL is sourced from BuildConfig — configured in build.gradle.kts.
 * Currently points to AWS EC2: http://65.1.86.83:8080/
 *
 * To change the backend URL, update the buildConfigField in build.gradle.kts.
 * When migrating to HTTPS, update both the URL and remove network_security_config.xml.
 */
object RetrofitClient {

    private const val TAG = "RetrofitClient"

    private lateinit var sessionManager: SessionManager
    private lateinit var _apiService: ApiService

    val apiService: ApiService
        get() {
            check(::_apiService.isInitialized) {
                "RetrofitClient.initialize(context) must be called before accessing apiService"
            }
            return _apiService
        }

    /**
     * Initialize the Retrofit client. Must be called once in MainActivity.onCreate().
     */
    fun initialize(context: Context) {
        if (::_apiService.isInitialized) return

        sessionManager = SessionManager.getInstance(context)

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🚀 API Base URL → ${BuildConfig.BASE_URL}")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            sessionManager.getAccessToken()?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        _apiService = retrofit.create(ApiService::class.java)

        Log.d(TAG, "✅ RetrofitClient initialized successfully")
    }
}
