package com.fairprice.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit API interface for the PDS Fair Price Go backend.
 */
interface ApiService {

    // ─── Auth ─────────────────────────────────────────────

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("api/v1/auth/register-profile")
    suspend fun registerProfile(@Body request: RegisterProfileRequest): Response<RegisterProfileResponse>

    // ─── LGD Hierarchy ──────────────────────────────────────

    @GET("api/v1/lgd/districts")
    suspend fun getDistricts(): Response<DistrictsResponse>

    @GET("api/v1/lgd/subdistricts")
    suspend fun getSubdistricts(@Query("district_code") districtCode: Int): Response<SubdistrictsResponse>

    @GET("api/v1/lgd/villages")
    suspend fun getVillages(@Query("subdistrict_code") subdistrictCode: Int): Response<VillagesResponse>
}
