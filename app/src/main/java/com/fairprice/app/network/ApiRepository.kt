package com.fairprice.app.network

/**
 * Repository layer — wraps every ApiService call with [safeApiCall]
 * to provide consistent [NetworkResult] error handling.
 *
 * All screens should call ApiRepository instead of RetrofitClient.apiService directly.
 */
object ApiRepository {

    private val api: ApiService
        get() = RetrofitClient.apiService

    // ─── Auth ─────────────────────────────────────────────────

    suspend fun login(request: LoginRequest): NetworkResult<LoginResponse> =
        safeApiCall { api.login(request) }

    suspend fun verifyOtp(request: VerifyOtpRequest): NetworkResult<VerifyOtpResponse> =
        safeApiCall { api.verifyOtp(request) }

    suspend fun officerLogin(request: OfficerLoginRequest): NetworkResult<OfficerLoginResponse> =
        safeApiCall { api.officerLogin(request) }

    suspend fun registerProfile(request: RegisterProfileRequest): NetworkResult<RegisterProfileResponse> =
        safeApiCall { api.registerProfile(request) }

    // ─── LGD Hierarchy ──────────────────────────────────────────

    suspend fun getDistricts(): NetworkResult<DistrictsResponse> =
        safeApiCall { api.getDistricts() }

    suspend fun getSubdistricts(districtCode: Int): NetworkResult<SubdistrictsResponse> =
        safeApiCall { api.getSubdistricts(districtCode) }

    suspend fun getVillages(subdistrictCode: Int): NetworkResult<VillagesResponse> =
        safeApiCall { api.getVillages(subdistrictCode) }

    // ─── Citizen Polls ──────────────────────────────────────────

    suspend fun getActivePolls(): NetworkResult<PollListResponse> =
        safeApiCall { api.getActivePolls() }

    suspend fun submitPoll(request: PollSubmitRequest): NetworkResult<PollSubmitResponse> =
        safeApiCall { api.submitPoll(request) }

    suspend fun getMyResponses(): NetworkResult<MyResponsesListResponse> =
        safeApiCall { api.getMyResponses() }

    // ─── Admin Polls ────────────────────────────────────────────

    suspend fun createPoll(request: CreatePollRequest): NetworkResult<CreatePollResponse> =
        safeApiCall { api.createPoll(request) }

    suspend fun getPollAnalytics(): NetworkResult<PollListResponse> =
        safeApiCall { api.getPollAnalytics() }

    suspend fun closePoll(id: String): NetworkResult<ClosePollResponse> =
        safeApiCall { api.closePoll(id) }

    // ─── Admin Analytics ─────────────────────────────────────────

    suspend fun getAnalyticsSummary(): NetworkResult<AnalyticsSummaryResponse> =
        safeApiCall { api.getAnalyticsSummary() }

    suspend fun getPollAnalyticsDetail(id: String): NetworkResult<PollAnalyticsDetailResponse> =
        safeApiCall { api.getPollAnalyticsDetail(id) }

    suspend fun getZoneClassification(): NetworkResult<ZoneClassificationResponse> =
        safeApiCall { api.getZoneClassification() }

    // ─── Admin Officers ──────────────────────────────────────────

    suspend fun getBlocks(districtCode: Int): NetworkResult<BlocksResponse> =
        safeApiCall { api.getBlocks(districtCode) }

    suspend fun createOfficer(request: CreateOfficerRequest): NetworkResult<CreateOfficerResponse> =
        safeApiCall { api.createOfficer(request) }

    suspend fun getOfficers(
        districtCode: Int? = null,
        blockCode: Int? = null,
        role: String? = null,
    ): NetworkResult<OfficerListResponse> =
        safeApiCall { api.getOfficers(districtCode, blockCode, role) }

    suspend fun updateOfficer(id: String, request: UpdateOfficerRequest): NetworkResult<UpdateOfficerResponse> =
        safeApiCall { api.updateOfficer(id, request) }

    suspend fun deactivateOfficer(id: String): NetworkResult<UpdateOfficerResponse> =
        safeApiCall { api.deactivateOfficer(id) }
}
