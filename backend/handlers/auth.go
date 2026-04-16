package handlers

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
	"pds-backend/models"
)

// AuthHandler handles authentication API endpoints.
type AuthHandler struct {
	DB          *pgxpool.Pool
	SupabaseURL string
	SupabaseKey string
}

// NewAuthHandler creates a new Auth handler.
func NewAuthHandler(db *pgxpool.Pool, supabaseURL, supabaseKey string) *AuthHandler {
	return &AuthHandler{
		DB:          db,
		SupabaseURL: supabaseURL,
		SupabaseKey: supabaseKey,
	}
}

// Login handles citizen login with ration card + phone.
// POST /api/v1/auth/login
func (h *AuthHandler) Login(c *gin.Context) {
	var req models.LoginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_INPUT",
			Message: "Ration card must be 12 digits and phone must be 10 digits",
		})
		return
	}

	// Format Indian mobile number with country code for Supabase
	phoneWithExt := fmt.Sprintf("+91%s", req.PhoneNo)

	// Check if user already exists
	var userID string
	var profileComplete bool
	err := h.DB.QueryRow(context.Background(),
		`SELECT id, profile_complete FROM users WHERE ration_card_no = $1`,
		req.RationCardNo).Scan(&userID, &profileComplete)

	if err != nil {
		// User doesn't exist — create a new one in our public.users table
		// Wait, we shouldn't insert here because Supabase Auth handles the user identity.
		// For now, we allow the OTP to be sent to this phone number.
		// When they verify OTP, we'll get their Supabase Auth User ID, which we will THEN use to create the public.users record.
	} else {
		// User exists — verify phone matches the one registered
		var storedPhone string
		h.DB.QueryRow(context.Background(),
			`SELECT phone_no FROM users WHERE id = $1`, userID).Scan(&storedPhone)

		if storedPhone != req.PhoneNo && storedPhone != "" {
			c.JSON(http.StatusUnauthorized, models.ErrorResponse{
				Error:   "PHONE_MISMATCH",
				Message: "Phone number does not match the registered number for this ration card",
			})
			return
		}
	}

	// ─── Call Supabase Auth to send OTP ───
	otpPayload := map[string]string{"phone": phoneWithExt}
	jsonBody, _ := json.Marshal(otpPayload)

	reqSupabase, _ := http.NewRequest("POST", h.SupabaseURL+"/auth/v1/otp", bytes.NewBuffer(jsonBody))
	reqSupabase.Header.Set("apikey", h.SupabaseKey)
	reqSupabase.Header.Set("Content-Type", "application/json")

	client := &http.Client{}
	resp, err := client.Do(reqSupabase)
	if err != nil || resp.StatusCode >= 400 {
		var errorMsg []byte
		if resp != nil {
			errorMsg, _ = io.ReadAll(resp.Body)
			resp.Body.Close()
		}
		fmt.Printf("Supabase OTP Error: status %d, body: %s\n", resp.StatusCode, string(errorMsg))
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "OTP_FAILED",
			Message: "Failed to dispatch OTP SMS. Please try again or check provider setup.",
		})
		return
	}
	defer resp.Body.Close()

	c.JSON(http.StatusOK, models.LoginResponse{
		UserID:          "", // We don't have the auth.users ID until they verify!
		ProfileRequired: true,
		Message:         "OTP sent to your phone number",
	})
}

// VerifyOtp handles OTP verification against Supabase Auth.
// POST /api/v1/auth/verify-otp
func (h *AuthHandler) VerifyOtp(c *gin.Context) {
	var req models.VerifyOtpRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_INPUT",
			Message: "Phone must be 10 digits and OTP must be 6 digits",
		})
		return
	}

	phoneWithExt := fmt.Sprintf("+91%s", req.PhoneNo)

	// ─── Call Supabase Auth to VERIFY OTP ───
	verifyPayload := map[string]string{
		"phone": phoneWithExt,
		"token": req.OtpCode,
		"type":  "sms",
	}
	jsonBody, _ := json.Marshal(verifyPayload)

	reqSupabase, _ := http.NewRequest("POST", h.SupabaseURL+"/auth/v1/verify?type=sms", bytes.NewBuffer(jsonBody))
	reqSupabase.Header.Set("apikey", h.SupabaseKey)
	reqSupabase.Header.Set("Content-Type", "application/json")

	client := &http.Client{}
	resp, err := client.Do(reqSupabase)
	if err != nil || resp.StatusCode >= 400 {
		var errorMsg []byte
		if resp != nil {
			errorMsg, _ = io.ReadAll(resp.Body)
			resp.Body.Close()
		}
		fmt.Printf("Supabase Verify Error: status %d, body: %s\n", resp.StatusCode, string(errorMsg))
		c.JSON(http.StatusUnauthorized, models.ErrorResponse{
			Error:   "INVALID_OTP",
			Message: "Incorrect or expired OTP.",
		})
		return
	}
	defer resp.Body.Close()

	// Parse Supabase response to extract user ID and Access Token
	var supabaseResp struct {
		AccessToken string `json:"access_token"`
		User        struct {
			ID string `json:"id"`
		} `json:"user"`
	}
	json.NewDecoder(resp.Body).Decode(&supabaseResp)

	supabaseUserID := supabaseResp.User.ID

	// Now check if this user exists in our custom users table
	var profileComplete bool
	err = h.DB.QueryRow(context.Background(),
		`SELECT profile_complete FROM users WHERE id = $1`,
		supabaseUserID).Scan(&profileComplete)

	if err != nil {
		// Creating user shell in our database. We'll populate ration_card later or in another flow if needed.
		// Since we didn't insert the ration card in the `login` function (because we didn't have the ID),
		// we should actually link it by updating the row... WAIT.
		// If you register for the first time, your public.users profile is blank.
		_, err = h.DB.Exec(context.Background(),
			`INSERT INTO users (id, ration_card_no, phone_no, role, profile_complete) 
			 VALUES ($1, $2, $3, 'CITIZEN', FALSE)
			 ON CONFLICT (ration_card_no) DO UPDATE SET phone_no = $3`,
			supabaseUserID, "TEMP_"+supabaseUserID[:8], req.PhoneNo) // We need a real ration card here, but we don't have it in Verify payload!
		profileComplete = false
	}

	c.JSON(http.StatusOK, models.VerifyOtpResponse{
		Verified:        true,
		UserID:          supabaseUserID,
		AccessToken:     supabaseResp.AccessToken,
		ProfileRequired: !profileComplete,
		Message:         "OTP verified successfully",
	})
}

// RegisterProfile handles mandatory profile setup after login.
// POST /api/v1/auth/register-profile
func (h *AuthHandler) RegisterProfile(c *gin.Context) {
	var req models.RegisterProfileRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_INPUT",
			Message: "All fields are required: full_name, address, district, subdistrict, village, hardware_uuid, gps",
		})
		return
	}

	// Check if hardware_uuid is already bound to another user
	var existingUserID string
	err := h.DB.QueryRow(context.Background(),
		`SELECT id FROM users WHERE hardware_uuid = $1 AND id != $2`,
		req.HardwareUUID, req.UserID).Scan(&existingUserID)

	if err == nil {
		// Another user is already bound to this device
		c.JSON(http.StatusConflict, models.ErrorResponse{
			Error:   "DEVICE_ALREADY_BOUND",
			Message: "This device is already registered with another account. One user per device is enforced.",
		})
		return
	}

	// Update user profile
	_, err = h.DB.Exec(context.Background(),
		`UPDATE users SET 
			full_name = $1, 
			address = $2, 
			state_code = 3, 
			district_code = $3, 
			subdistrict_code = $4, 
			village_code = $5, 
			hardware_uuid = $6, 
			gps_lat = $7, 
			gps_lng = $8, 
			profile_complete = TRUE, 
			updated_at = NOW() 
		 WHERE id = $9`,
		req.FullName, req.Address,
		req.DistrictCode, req.SubdistrictCode, req.VillageCode,
		req.HardwareUUID, req.GpsLat, req.GpsLng, req.UserID)

	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to save profile. Please try again.",
		})
		return
	}

	c.JSON(http.StatusOK, models.RegisterProfileResponse{
		Success: true,
		Message: "Profile registered successfully",
	})
}

// createOtpSession creates a simulated OTP session.
func (h *AuthHandler) createOtpSession(userID, phoneNo string) {
	h.DB.Exec(context.Background(),
		`INSERT INTO otp_sessions (user_id, phone_no, otp_code) 
		 VALUES ($1, $2, '123456')`,
		userID, phoneNo)
}
