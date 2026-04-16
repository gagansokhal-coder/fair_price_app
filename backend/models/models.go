package models

// ─── LGD Hierarchy ──────────────────────────────────────────

// LgdItem represents a single entry in the LGD hierarchy dropdown.
type LgdItem struct {
	Code int    `json:"code"`
	Name string `json:"name"`
}

// ─── Auth Request/Response ──────────────────────────────────

// LoginRequest — Citizen login with ration card + phone.
type LoginRequest struct {
	RationCardNo string `json:"ration_card_no" binding:"required,len=12"`
	PhoneNo      string `json:"phone_no" binding:"required,len=10"`
}

// LoginResponse after successful login initiation.
type LoginResponse struct {
	UserID          string `json:"user_id"`
	ProfileRequired bool   `json:"profile_required"`
	Message         string `json:"message"`
}

// VerifyOtpRequest — Simulated OTP verification.
type VerifyOtpRequest struct {
	PhoneNo string `json:"phone_no" binding:"required,len=10"`
	OtpCode string `json:"otp_code" binding:"required,len=6"`
}

// VerifyOtpResponse after OTP verification.
type VerifyOtpResponse struct {
	Verified        bool   `json:"verified"`
	UserID          string `json:"user_id"`
	AccessToken     string `json:"access_token"`
	ProfileRequired bool   `json:"profile_required"`
	Message         string `json:"message"`
}

// RegisterProfileRequest — Mandatory profile setup after login.
type RegisterProfileRequest struct {
	UserID          string  `json:"user_id" binding:"required"`
	FullName        string  `json:"full_name" binding:"required"`
	Address         string  `json:"address" binding:"required"`
	DistrictCode    int     `json:"district_code" binding:"required"`
	SubdistrictCode int     `json:"subdistrict_code" binding:"required"`
	VillageCode     int     `json:"village_code" binding:"required"`
	HardwareUUID    string  `json:"hardware_uuid" binding:"required"`
	GpsLat          float64 `json:"gps_lat" binding:"required"`
	GpsLng          float64 `json:"gps_lng" binding:"required"`
}

// RegisterProfileResponse after profile completion.
type RegisterProfileResponse struct {
	Success bool   `json:"success"`
	Message string `json:"message"`
}

// ─── User ───────────────────────────────────────────────────

// User represents a registered citizen.
type User struct {
	ID              string  `json:"id"`
	RationCardNo    string  `json:"ration_card_no"`
	PhoneNo         string  `json:"phone_no"`
	FullName        string  `json:"full_name,omitempty"`
	Address         string  `json:"address,omitempty"`
	Role            string  `json:"role"`
	StateCode       int     `json:"state_code,omitempty"`
	DistrictCode    int     `json:"district_code,omitempty"`
	SubdistrictCode int     `json:"subdistrict_code,omitempty"`
	VillageCode     int     `json:"village_code,omitempty"`
	HardwareUUID    string  `json:"hardware_uuid,omitempty"`
	GpsLat          float64 `json:"gps_lat,omitempty"`
	GpsLng          float64 `json:"gps_lng,omitempty"`
	ProfileComplete bool    `json:"profile_complete"`
}

// ErrorResponse standard error payload.
type ErrorResponse struct {
	Error   string `json:"error"`
	Message string `json:"message"`
}
