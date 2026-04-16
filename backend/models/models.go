package models

// ─── LGD Hierarchy ──────────────────────────────────────────

// LgdItem represents a single entry in the LGD hierarchy dropdown.
type LgdItem struct {
	Code int    `json:"code"`
	Name string `json:"name"`
}

// ─── Auth Request/Response ──────────────────────────────────

// LoginRequest — Citizen login with ration card + phone + GPS.
type LoginRequest struct {
	RationCardNo string  `json:"ration_card_no" binding:"required,len=12"`
	PhoneNo      string  `json:"phone_no" binding:"required,len=10"`
	GpsLat       float64 `json:"gps_lat"`
	GpsLng       float64 `json:"gps_lng"`
}

// LoginResponse after successful login initiation.
type LoginResponse struct {
	UserID          string `json:"user_id"`
	ProfileRequired bool   `json:"profile_required"`
	GpsVerified     bool   `json:"gps_verified"`
	Message         string `json:"message"`
}

// VerifyOtpRequest — OTP verification.
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

// ─── Custom Poll Models ─────────────────────────────────────

// CreatePollRequest — Officer creates a dynamic poll.
type CreatePollRequest struct {
	Title       string   `json:"title" binding:"required"`
	Description string   `json:"description"`
	TargetLevel string   `json:"target_level" binding:"required"` // DISTRICT, SUBDIVISION, BLOCK, VILLAGE
	TargetCode  int      `json:"target_code" binding:"required"`
	Options     []string `json:"options" binding:"required,min=2,max=5"`
}

// CreatePollResponse after poll creation.
type CreatePollResponse struct {
	Success bool   `json:"success"`
	PollID  string `json:"poll_id"`
	Message string `json:"message"`
}

// CustomPoll — A dynamic poll visible to targeted citizens.
type CustomPoll struct {
	PollID      string   `json:"poll_id"`
	Title       string   `json:"title"`
	Description string   `json:"description,omitempty"`
	TargetLevel string   `json:"target_level"`
	TargetCode  int      `json:"target_code"`
	Options     []string `json:"options"`
	IsActive    bool     `json:"is_active"`
	CreatedAt   string   `json:"created_at"`
	CreatedBy   string   `json:"created_by,omitempty"`
	// Analytics (populated for admin view)
	TotalResponses int                `json:"total_responses,omitempty"`
	OptionCounts   map[string]int     `json:"option_counts,omitempty"`
}

// PollListResponse wraps a list of polls.
type PollListResponse struct {
	Polls []CustomPoll `json:"polls"`
	Total int          `json:"total"`
}

// PollSubmitRequest — Citizen submits a poll response with GPS.
type PollSubmitRequest struct {
	PollID              string  `json:"poll_id" binding:"required"`
	SelectedOptionIndex int     `json:"selected_option_index" binding:"min=0,max=4"`
	GpsLat              float64 `json:"gps_lat"`
	GpsLng              float64 `json:"gps_lng"`
}

// PollSubmitResponse after successful poll submission.
type PollSubmitResponse struct {
	Success  bool    `json:"success"`
	Distance float64 `json:"distance_meters,omitempty"`
	Message  string  `json:"message"`
}

// PollAnalytics — Detailed analytics for a single poll.
type PollAnalytics struct {
	PollID         string         `json:"poll_id"`
	Title          string         `json:"title"`
	TotalResponses int            `json:"total_responses"`
	TargetLevel    string         `json:"target_level"`
	TargetCode     int            `json:"target_code"`
	Options        []string       `json:"options"`
	OptionCounts   map[string]int `json:"option_counts"`
	CreatedAt      string         `json:"created_at"`
}

// MyPollResponse — A single past vote by the citizen.
type MyPollResponse struct {
	ResponseID          string  `json:"response_id"`
	PollID              string  `json:"poll_id"`
	PollTitle           string  `json:"poll_title"`
	PollActive          bool    `json:"poll_active"`
	SelectedOptionIndex int     `json:"selected_option_index"`
	SelectedOptionText  string  `json:"selected_option_text"`
	GpsLat              float64 `json:"gps_lat,omitempty"`
	GpsLng              float64 `json:"gps_lng,omitempty"`
	DistanceMeters      float64 `json:"distance_meters,omitempty"`
	SubmittedAt         string  `json:"submitted_at"`
}

// MyResponsesListResponse wraps a citizen's voting history.
type MyResponsesListResponse struct {
	Responses []MyPollResponse `json:"responses"`
	Total     int              `json:"total"`
}

// ─── Analytics Engine Models ────────────────────────────────

// ZoneEntry represents a single area with its zone classification.
type ZoneEntry struct {
	AreaCode       int     `json:"area_code"`
	AreaName       string  `json:"area_name"`
	AreaLevel      string  `json:"area_level"` // VILLAGE, BLOCK, SUBDIVISION, DISTRICT
	Zone           string  `json:"zone"`       // GREEN, YELLOW, RED
	TotalResponses int     `json:"total_responses"`
	PositiveCount  int     `json:"positive_count"`
	PositivePct    float64 `json:"positive_pct"` // e.g. 72.5
	DistrictName   string  `json:"district_name,omitempty"`
	ParentName     string  `json:"parent_name,omitempty"` // Block name for villages
}

// PieChartData — Option-wise percentage distribution for frontend pie chart.
type PieChartData struct {
	Labels      []string  `json:"labels"`
	Values      []int     `json:"values"`
	Percentages []float64 `json:"percentages"`
}

// BarChartSeries — One data series (one option) in a bar chart.
type BarChartSeries struct {
	Label  string `json:"label"`  // Option text
	Values []int  `json:"values"` // Count per area (matches Categories order)
}

// BarChartData — Per-area comparison for frontend bar chart.
type BarChartData struct {
	Categories []string         `json:"categories"` // Area names
	Series     []BarChartSeries `json:"series"`      // One per option
}

// AnalyticsSummaryResponse — GET /api/v1/admin/analytics/summary
type AnalyticsSummaryResponse struct {
	TotalResponses int         `json:"total_responses"`
	TotalZones     int         `json:"total_zones"`
	GreenZones     int         `json:"green_zones"`
	YellowZones    int         `json:"yellow_zones"`
	RedZones       int         `json:"red_zones"`
	AvgPositivePct float64     `json:"avg_positive_pct"`
	Zones          []ZoneEntry `json:"zones"`
}

// PollAnalyticsDetailResponse — GET /api/v1/admin/analytics/poll/:id
type PollAnalyticsDetailResponse struct {
	PollID         string       `json:"poll_id"`
	Title          string       `json:"title"`
	TargetLevel    string       `json:"target_level"`
	TargetCode     int          `json:"target_code"`
	Options        []string     `json:"options"`
	IsActive       bool         `json:"is_active"`
	TotalResponses int          `json:"total_responses"`
	PieChart       PieChartData `json:"pie_chart"`
	BarChart       BarChartData `json:"bar_chart"`
	ZoneMap        []ZoneEntry  `json:"zone_map"`
}

// ZoneClassificationResponse — GET /api/v1/admin/analytics/zones
type ZoneClassificationResponse struct {
	TotalZones  int         `json:"total_zones"`
	GreenZones  int         `json:"green_zones"`
	YellowZones int         `json:"yellow_zones"`
	RedZones    int         `json:"red_zones"`
	Zones       []ZoneEntry `json:"zones"`
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
	FpsID           string  `json:"fps_id,omitempty"`
	GpsLat          float64 `json:"gps_lat,omitempty"`
	GpsLng          float64 `json:"gps_lng,omitempty"`
	ProfileComplete bool    `json:"profile_complete"`
}

// ErrorResponse standard error payload.
type ErrorResponse struct {
	Error   string `json:"error"`
	Message string `json:"message"`
}

// ─── RBAC Constants ─────────────────────────────────────────

const (
	RoleAdminState       = "ADMIN_STATE"
	RoleAdminDistrict    = "ADMIN_DISTRICT"
	RoleAdminSubdivision = "ADMIN_SUBDIVISION"
	RoleAdminBlock       = "ADMIN_BLOCK"
	RoleCitizen          = "CITIZEN"
)

// RoleHierarchy defines the numeric weight for RBAC comparisons.
// Higher number = higher authority.
var RoleHierarchy = map[string]int{
	RoleCitizen:          0,
	RoleAdminBlock:       1,
	RoleAdminSubdivision: 2,
	RoleAdminDistrict:    3,
	RoleAdminState:       4,
}

// ─── Officer Models ─────────────────────────────────────────

// Officer represents an administrative officer in the hierarchy.
type Officer struct {
	ID               string  `json:"id"`
	Name             string  `json:"name"`
	PhoneNo          string  `json:"phone_no"`
	Email            string  `json:"email,omitempty"`
	Role             string  `json:"role"`
	StateCode        int     `json:"state_code"`
	DistrictCode     *int    `json:"district_code,omitempty"`
	SubdistrictCode  *int    `json:"subdistrict_code,omitempty"`
	BlockCode        *int    `json:"block_code,omitempty"`
	Designation      string  `json:"designation,omitempty"`
	DistrictName     string  `json:"district_name,omitempty"`
	SubdistrictName  string  `json:"subdistrict_name,omitempty"`
	CreatedByName    string  `json:"created_by_name,omitempty"`
	IsActive         bool    `json:"is_active"`
	CreatedAt        string  `json:"created_at,omitempty"`
}

// CreateOfficerRequest — Admin creates a new officer.
type CreateOfficerRequest struct {
	Name            string `json:"name" binding:"required"`
	PhoneNo         string `json:"phone_no" binding:"required,len=10"`
	Email           string `json:"email"`
	Role            string `json:"role" binding:"required"`
	DistrictCode    *int   `json:"district_code"`
	SubdistrictCode *int   `json:"subdistrict_code"`
	BlockCode       *int   `json:"block_code"`
	Designation     string `json:"designation"`
}

// UpdateOfficerRequest — Admin updates an existing officer.
type UpdateOfficerRequest struct {
	Name            string `json:"name"`
	Email           string `json:"email"`
	Role            string `json:"role"`
	DistrictCode    *int   `json:"district_code"`
	SubdistrictCode *int   `json:"subdistrict_code"`
	BlockCode       *int   `json:"block_code"`
	Designation     string `json:"designation"`
	IsActive        *bool  `json:"is_active"`
}

// OfficerListResponse wraps a list of officers.
type OfficerListResponse struct {
	Officers []Officer `json:"officers"`
	Total    int       `json:"total"`
}

// OfficerResponse wraps a single officer.
type OfficerResponse struct {
	Success bool    `json:"success"`
	Officer Officer `json:"officer"`
	Message string  `json:"message"`
}

// LgdBlock represents a block from the convenience view.
type LgdBlock struct {
	BlockCode    int    `json:"block_code"`
	BlockName    string `json:"block_name"`
	DistrictCode int    `json:"district_code"`
	DistrictName string `json:"district_name"`
}
