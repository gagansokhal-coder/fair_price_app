package com.fairprice.app.network

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

// ─── Auth ─────────────────────────────────────────────────

data class LoginRequest(
    @SerializedName("ration_card_no") val rationCardNo: String,
    @SerializedName("phone_no") val phoneNo: String,
)

data class LoginResponse(
    @SerializedName("user_id") val userId: String,
    @SerializedName("profile_required") val profileRequired: Boolean,
    @SerializedName("message") val message: String,
)

data class VerifyOtpRequest(
    @SerializedName("phone_no") val phoneNo: String,
    @SerializedName("otp_code") val otpCode: String,
)

data class VerifyOtpResponse(
    @SerializedName("verified") val verified: Boolean,
    @SerializedName("user_id") val userId: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("profile_required") val profileRequired: Boolean,
    @SerializedName("message") val message: String,
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

data class ErrorResponse(
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String,
)
