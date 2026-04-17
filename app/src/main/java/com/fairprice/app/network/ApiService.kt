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

    @POST("api/v1/auth/officer-login")
    suspend fun officerLogin(@Body request: OfficerLoginRequest): Response<OfficerLoginResponse>

    @POST("api/v1/auth/register-profile")
    suspend fun registerProfile(@Body request: RegisterProfileRequest): Response<RegisterProfileResponse>

    // ─── LGD Hierarchy ──────────────────────────────────────

    @GET("api/v1/lgd/districts")
    suspend fun getDistricts(): Response<DistrictsResponse>

    @GET("api/v1/lgd/subdistricts")
    suspend fun getSubdistricts(@Query("district_code") districtCode: Int): Response<SubdistrictsResponse>

    @GET("api/v1/lgd/villages")
    suspend fun getVillages(@Query("subdistrict_code") subdistrictCode: Int): Response<VillagesResponse>

    // ─── Citizen Polls ──────────────────────────────────────

    @GET("api/v1/polls")
    suspend fun getActivePolls(): Response<PollListResponse>

    @POST("api/v1/polls/submit")
    suspend fun submitPoll(@Body request: PollSubmitRequest): Response<PollSubmitResponse>

    @GET("api/v1/polls/my-responses")
    suspend fun getMyResponses(): Response<MyResponsesListResponse>

    // ─── Admin Polls ────────────────────────────────────────

    @POST("api/v1/admin/create-poll")
    suspend fun createPoll(@Body request: CreatePollRequest): Response<CreatePollResponse>

    @GET("api/v1/admin/polls")
    suspend fun getPollAnalytics(): Response<PollListResponse>

    @retrofit2.http.PATCH("api/v1/admin/poll/{id}/close")
    suspend fun closePoll(@retrofit2.http.Path("id") id: String): Response<ClosePollResponse>

    // ─── Admin Analytics ─────────────────────────────────────

    @GET("api/v1/admin/analytics/summary")
    suspend fun getAnalyticsSummary(): Response<AnalyticsSummaryResponse>

    @GET("api/v1/admin/analytics/poll/{id}")
    suspend fun getPollAnalyticsDetail(@retrofit2.http.Path("id") id: String): Response<PollAnalyticsDetailResponse>

    @GET("api/v1/admin/analytics/zones")
    suspend fun getZoneClassification(): Response<ZoneClassificationResponse>

    // ─── Admin Officers ──────────────────────────────────────

    @GET("api/v1/admin/blocks")
    suspend fun getBlocks(@Query("district_code") districtCode: Int): Response<BlocksResponse>

    @POST("api/v1/admin/create-officer")
    suspend fun createOfficer(@Body request: CreateOfficerRequest): Response<CreateOfficerResponse>

    @GET("api/v1/admin/officers")
    suspend fun getOfficers(
        @Query("district_code") districtCode: Int? = null,
        @Query("block_code") blockCode: Int? = null,
        @Query("role") role: String? = null
    ): Response<OfficerListResponse>

    @retrofit2.http.PUT("api/v1/admin/officer/{id}")
    suspend fun updateOfficer(
        @retrofit2.http.Path("id") id: String,
        @Body request: UpdateOfficerRequest
    ): Response<UpdateOfficerResponse>

    @retrofit2.http.PATCH("api/v1/admin/officer/{id}/deactivate")
    suspend fun deactivateOfficer(@retrofit2.http.Path("id") id: String): Response<UpdateOfficerResponse>
}
