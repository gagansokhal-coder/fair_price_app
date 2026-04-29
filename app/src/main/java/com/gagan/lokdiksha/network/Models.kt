package com.gagan.lokdiksha.network

import com.google.gson.annotations.SerializedName

// ─── LGD Hierarchy ──────────────────────────────────────────

data class LgdItem(
    @SerializedName("code") val code: Int,
    @SerializedName("name") val name: String,
)

data class DistrictsResponse(
    @SerializedName("districts") val districts: List<LgdItem>,
)

data class SubdistrictsResponse(
    @SerializedName("subdistricts") val subdistricts: List<LgdItem>,
)

data class VillagesResponse(
    @SerializedName("villages") val villages: List<LgdItem>,
)

data class LgdBlock(
    @SerializedName("block_code") val blockCode: Int,
    @SerializedName("block_name") val blockName: String,
    @SerializedName("district_code") val districtCode: Int,
    @SerializedName("district_name") val districtName: String,
)

data class BlocksResponse(
    @SerializedName("blocks") val blocks: List<LgdBlock>,
)

// ─── Auth ─────────────────────────────────────────────────

data class LoginRequest(
    @SerializedName("ration_card_no") val rationCardNo: String,
    @SerializedName("phone_no") val phoneNo: String,
    @SerializedName("hardware_uuid") val hardwareUuid: String = "",
    @SerializedName("gps_lat") val gpsLat: Double = 0.0,
    @SerializedName("gps_lng") val gpsLng: Double = 0.0,
)

data class LoginResponse(
    @SerializedName("user_id") val userId: String,
    @SerializedName("profile_required") val profileRequired: Boolean,
    @SerializedName("gps_verified") val gpsVerified: Boolean,
    @SerializedName("message") val message: String,
)

data class VerifyOtpRequest(
    @SerializedName("phone_no") val phoneNo: String,
    @SerializedName("otp_code") val otpCode: String,
    @SerializedName("ration_card_no") val rationCardNo: String = "",
)

data class VerifyOtpResponse(
    @SerializedName("verified") val verified: Boolean,
    @SerializedName("user_id") val userId: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("profile_required") val profileRequired: Boolean,
    @SerializedName("message") val message: String,
    // Citizen profile fields for returning users
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("district_name") val districtName: String = "",
    @SerializedName("subdistrict_name") val subdistrictName: String = "",
    @SerializedName("village_name") val villageName: String = "",
)

data class RegisterProfileRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("address") val address: String,
    @SerializedName("district_code") val districtCode: Int,
    @SerializedName("subdistrict_code") val subdistrictCode: Int,
    @SerializedName("village_code") val villageCode: Int,
    @SerializedName("hardware_uuid") val hardwareUuid: String,
    @SerializedName("gps_lat") val gpsLat: Double,
    @SerializedName("gps_lng") val gpsLng: Double,
)

data class RegisterProfileResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
)

// ─── Custom Polls ─────────────────────────────────────────

data class CreatePollRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("target_level") val targetLevel: String,
    @SerializedName("target_code") val targetCode: Int,
    @SerializedName("options") val options: List<String>,
)

data class CreatePollResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("poll_id") val pollId: String,
    @SerializedName("message") val message: String,
)

data class CustomPoll(
    @SerializedName("poll_id") val pollId: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("target_level") val targetLevel: String,
    @SerializedName("target_code") val targetCode: Int,
    @SerializedName("options") val options: List<String>,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("total_responses") val totalResponses: Int = 0,
    @SerializedName("option_counts") val optionCounts: Map<String, Int>? = null,
)

data class PollListResponse(
    @SerializedName("polls") val polls: List<CustomPoll>,
    @SerializedName("total") val total: Int,
)

data class PollSubmitRequest(
    @SerializedName("poll_id") val pollId: String,
    @SerializedName("selected_option_index") val selectedOptionIndex: Int,
    @SerializedName("gps_lat") val gpsLat: Double = 0.0,
    @SerializedName("gps_lng") val gpsLng: Double = 0.0,
)

data class PollSubmitResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("distance_meters") val distanceMeters: Double?,
    @SerializedName("message") val message: String,
)

data class ClosePollResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
)

// Citizen's past vote on a poll
data class MyPollResponse(
    @SerializedName("response_id") val responseId: String,
    @SerializedName("poll_id") val pollId: String,
    @SerializedName("poll_title") val pollTitle: String,
    @SerializedName("poll_active") val pollActive: Boolean,
    @SerializedName("selected_option_index") val selectedOptionIndex: Int,
    @SerializedName("selected_option_text") val selectedOptionText: String,
    @SerializedName("gps_lat") val gpsLat: Double?,
    @SerializedName("gps_lng") val gpsLng: Double?,
    @SerializedName("distance_meters") val distanceMeters: Double?,
    @SerializedName("submitted_at") val submittedAt: String,
)

data class MyResponsesListResponse(
    @SerializedName("responses") val responses: List<MyPollResponse>,
    @SerializedName("total") val total: Int,
)

// Generic server error body for parsing non-2xx responses
data class ApiErrorResponse(
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String,
)

// ─── Analytics Engine Models ──────────────────────────────

data class ZoneEntry(
    @SerializedName("area_code") val areaCode: Int,
    @SerializedName("area_name") val areaName: String,
    @SerializedName("area_level") val areaLevel: String,
    @SerializedName("zone") val zone: String, // GREEN, YELLOW, RED
    @SerializedName("total_responses") val totalResponses: Int,
    @SerializedName("positive_count") val positiveCount: Int,
    @SerializedName("positive_pct") val positivePct: Double,
    @SerializedName("district_name") val districtName: String? = null,
    @SerializedName("parent_name") val parentName: String? = null,
)

data class PieChartData(
    @SerializedName("labels") val labels: List<String>,
    @SerializedName("values") val values: List<Int>,
    @SerializedName("percentages") val percentages: List<Double>,
)

data class BarChartSeries(
    @SerializedName("label") val label: String,
    @SerializedName("values") val values: List<Int>,
)

data class BarChartData(
    @SerializedName("categories") val categories: List<String>,
    @SerializedName("series") val series: List<BarChartSeries>,
)

data class AnalyticsSummaryResponse(
    @SerializedName("total_responses") val totalResponses: Int,
    @SerializedName("total_zones") val totalZones: Int,
    @SerializedName("green_zones") val greenZones: Int,
    @SerializedName("yellow_zones") val yellowZones: Int,
    @SerializedName("red_zones") val redZones: Int,
    @SerializedName("avg_positive_pct") val avgPositivePct: Double,
    @SerializedName("zones") val zones: List<ZoneEntry>,
)

data class PollAnalyticsDetailResponse(
    @SerializedName("poll_id") val pollId: String,
    @SerializedName("title") val title: String,
    @SerializedName("target_level") val targetLevel: String,
    @SerializedName("target_code") val targetCode: Int,
    @SerializedName("options") val options: List<String>,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("total_responses") val totalResponses: Int,
    @SerializedName("pie_chart") val pieChart: PieChartData,
    @SerializedName("bar_chart") val barChart: BarChartData,
    @SerializedName("zone_map") val zoneMap: List<ZoneEntry>,
)

data class ZoneClassificationResponse(
    @SerializedName("total_zones") val totalZones: Int,
    @SerializedName("green_zones") val greenZones: Int,
    @SerializedName("yellow_zones") val yellowZones: Int,
    @SerializedName("red_zones") val redZones: Int,
    @SerializedName("zones") val zones: List<ZoneEntry>,
)

// ─── Officers ─────────────────────────────────────────────

data class Officer(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone_no") val phoneNo: String,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String,
    @SerializedName("state_code") val stateCode: Int,
    @SerializedName("district_code") val districtCode: Int?,
    @SerializedName("subdistrict_code") val subdistrictCode: Int?,
    @SerializedName("block_code") val blockCode: Int?,
    @SerializedName("designation") val designation: String?,
    @SerializedName("district_name") val districtName: String?,
    @SerializedName("subdistrict_name") val subdistrictName: String?,
    @SerializedName("created_by_name") val createdByName: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String?,
)

data class CreateOfficerRequest(
    @SerializedName("name") val name: String,
    @SerializedName("phone_no") val phoneNo: String,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String,
    @SerializedName("district_code") val districtCode: Int?,
    @SerializedName("subdistrict_code") val subdistrictCode: Int?,
    @SerializedName("block_code") val blockCode: Int?,
    @SerializedName("designation") val designation: String?,
)

data class CreateOfficerResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("officer") val officer: Officer,
    @SerializedName("message") val message: String,
)

data class OfficerListResponse(
    @SerializedName("officers") val officers: List<Officer>,
    @SerializedName("total") val total: Int,
)

data class UpdateOfficerRequest(
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("district_code") val districtCode: Int?,
    @SerializedName("subdistrict_code") val subdistrictCode: Int?,
    @SerializedName("block_code") val blockCode: Int?,
    @SerializedName("designation") val designation: String?,
    @SerializedName("is_active") val isActive: Boolean?,
)

data class UpdateOfficerResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
)

data class ErrorResponse(
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String,
)

// ─── Officer Login ─────────────────────────────────────────────

data class OfficerLoginRequest(
    @SerializedName("phone_no") val phoneNo: String,
    @SerializedName("password") val password: String,
)

data class OfficerLoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("officer_id") val officerId: String,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String,
    @SerializedName("designation") val designation: String,
    @SerializedName("district_name") val districtName: String?,
    @SerializedName("message") val message: String,
)
