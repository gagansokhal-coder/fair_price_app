package handlers

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/jackc/pgx/v5/pgxpool"
	"pds-backend/models"
)

// PollHandler handles poll-related API endpoints.
type PollHandler struct {
	DB *pgxpool.Pool
}

// NewPollHandler creates a new Poll handler.
func NewPollHandler(db *pgxpool.Pool) *PollHandler {
	return &PollHandler{DB: db}
}

// ═══════════════════════════════════════════════════════════════
// ADMIN: CreatePoll — Hierarchy-scoped dynamic poll creation
// POST /api/v1/admin/create-poll
// ═══════════════════════════════════════════════════════════════
func (h *PollHandler) CreatePoll(c *gin.Context) {
	// Recover from any panics (nil type assertions in jurisdiction validation)
	defer func() {
		if r := recover(); r != nil {
			fmt.Printf("[PANIC] CreatePoll recovered: %v\n", r)
			c.JSON(http.StatusInternalServerError, models.ErrorResponse{
				Error:   "INTERNAL_ERROR",
				Message: "An unexpected server error occurred. Please try again.",
			})
		}
	}()

	var req models.CreatePollRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_INPUT",
			Message: "title, target_level, target_code, and options (2-5) are required",
		})
		return
	}

	// Validate options count
	if len(req.Options) < 2 || len(req.Options) > 5 {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_OPTIONS",
			Message: "Polls must have between 2 and 5 options",
		})
		return
	}

	// Validate target_level
	validLevels := map[string]bool{
		"DISTRICT": true, "SUBDIVISION": true, "BLOCK": true, "VILLAGE": true,
	}
	if !validLevels[req.TargetLevel] {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_TARGET",
			Message: "target_level must be DISTRICT, SUBDIVISION, BLOCK, or VILLAGE",
		})
		return
	}

	// ─── Jurisdiction Validation ────────────────────────────
	// Officers can only create polls within their assigned LGD area.
	officerRole, _ := c.Get("officerRole")
	role := fmt.Sprintf("%v", officerRole)
	officerID, _ := c.Get("officerID")
	officerIDStr := fmt.Sprintf("%v", officerID)

	fmt.Printf("[DEBUG] CreatePoll: officer=%s role=%s target=%s:%d\n", officerIDStr, role, req.TargetLevel, req.TargetCode)

	if !h.validatePollJurisdiction(c, role, req.TargetLevel, req.TargetCode) {
		c.JSON(http.StatusForbidden, models.ErrorResponse{
			Error:   "JURISDICTION_VIOLATION",
			Message: fmt.Sprintf("You (%s) cannot create polls targeting %s:%d — it is outside your assigned jurisdiction", role, req.TargetLevel, req.TargetCode),
		})
		return
	}

	// Verify the target LGD code actually exists
	if !h.verifyLgdTarget(req.TargetLevel, req.TargetCode) {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_LGD_CODE",
			Message: fmt.Sprintf("No %s found with code %d in LGD hierarchy", req.TargetLevel, req.TargetCode),
		})
		return
	}

	// Marshal options to JSONB
	optionsJSON, err := json.Marshal(req.Options)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "INTERNAL_ERROR",
			Message: "Failed to process poll options",
		})
		return
	}

	// Insert the poll
	var pollID string
	err = h.DB.QueryRow(context.Background(),
		`INSERT INTO custom_polls (title, description, created_by, target_level, target_code, options)
		 VALUES ($1, $2, $3::uuid, $4, $5, $6)
		 RETURNING poll_id`,
		req.Title, req.Description, officerIDStr,
		req.TargetLevel, req.TargetCode, optionsJSON).Scan(&pollID)

	if err != nil {
		fmt.Printf("[ERROR] Poll creation DB error: officer=%s target=%s:%d err=%v\n", officerIDStr, req.TargetLevel, req.TargetCode, err)
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to create poll. Please verify your inputs and try again.",
		})
		return
	}

	fmt.Printf("[INFO] Poll created: id=%s title='%s' target=%s:%d by=%s\n", pollID, req.Title, req.TargetLevel, req.TargetCode, officerIDStr)
	c.JSON(http.StatusCreated, models.CreatePollResponse{
		Success: true,
		PollID:  pollID,
		Message: fmt.Sprintf("Poll '%s' created targeting %s:%d", req.Title, req.TargetLevel, req.TargetCode),
	})
}

// validatePollJurisdiction enforces hierarchy-based poll creation rules.
//
// Rules:
//   - ADMIN_STATE:       Can poll anywhere in the state
//   - ADMIN_DISTRICT:    Can poll any subdivision/block/village in their district
//   - ADMIN_SUBDIVISION: Can poll any block/village under their subdivision
//   - ADMIN_BLOCK:       Can poll only villages within their block
func (h *PollHandler) validatePollJurisdiction(c *gin.Context, role, targetLevel string, targetCode int) bool {

	switch role {
	case models.RoleAdminState:
		return true // Full state access

	case models.RoleAdminDistrict:
		callerDistrict, exists := c.Get("officerDistrictCode")
		if !exists {
			fmt.Printf("[WARN] validatePollJurisdiction: officerDistrictCode not set for ADMIN_DISTRICT\n")
			return false
		}
		districtCode, ok := callerDistrict.(int)
		if !ok {
			fmt.Printf("[WARN] validatePollJurisdiction: officerDistrictCode is not int: %T\n", callerDistrict)
			return false
		}
		return h.isCodeInDistrict(targetLevel, targetCode, districtCode)

	case models.RoleAdminSubdivision:
		callerDistrict, d := c.Get("officerDistrictCode")
		callerSubdistrict, s := c.Get("officerSubdistrictCode")
		if !d || !s {
			fmt.Printf("[WARN] validatePollJurisdiction: missing codes for ADMIN_SUBDIVISION (district=%v, subdistrict=%v)\n", d, s)
			return false
		}
		dCode, ok1 := callerDistrict.(int)
		sCode, ok2 := callerSubdistrict.(int)
		if !ok1 || !ok2 {
			fmt.Printf("[WARN] validatePollJurisdiction: type mismatch for ADMIN_SUBDIVISION codes\n")
			return false
		}
		return h.isCodeInSubdivision(targetLevel, targetCode, dCode, sCode)

	case models.RoleAdminBlock:
		callerBlock, exists := c.Get("officerBlockCode")
		if !exists {
			fmt.Printf("[WARN] validatePollJurisdiction: officerBlockCode not set for ADMIN_BLOCK\n")
			return false
		}
		blockCode, ok := callerBlock.(int)
		if !ok {
			fmt.Printf("[WARN] validatePollJurisdiction: officerBlockCode is not int: %T\n", callerBlock)
			return false
		}
		// BDOs can only target villages within their block
		if targetLevel == "VILLAGE" {
			return h.isVillageInBlock(targetCode, blockCode)
		}
		// BDOs cannot create district/subdivision/block-level polls
		return false

	default:
		return false
	}
}

// isCodeInDistrict checks if targetCode (at targetLevel) belongs to districtCode.
func (h *PollHandler) isCodeInDistrict(targetLevel string, targetCode, districtCode int) bool {
	var exists bool
	var query string

	switch targetLevel {
	case "DISTRICT":
		return targetCode == districtCode
	case "SUBDIVISION":
		query = `SELECT EXISTS(SELECT 1 FROM lgd_hierarchy WHERE district_code = $1 AND subdistrict_code = $2 LIMIT 1)`
	case "BLOCK":
		query = `SELECT EXISTS(SELECT 1 FROM lgd_hierarchy WHERE district_code = $1 AND subdistrict_code = $2 LIMIT 1)`
	case "VILLAGE":
		query = `SELECT EXISTS(SELECT 1 FROM lgd_hierarchy WHERE district_code = $1 AND village_code = $2 LIMIT 1)`
	default:
		return false
	}

	h.DB.QueryRow(context.Background(), query, districtCode, targetCode).Scan(&exists)
	return exists
}

// isCodeInSubdivision checks if targetCode belongs to the officer's subdivision.
func (h *PollHandler) isCodeInSubdivision(targetLevel string, targetCode, districtCode, subdistrictCode int) bool {
	var exists bool

	switch targetLevel {
	case "DISTRICT":
		return false // SDOs cannot target entire districts
	case "SUBDIVISION":
		return targetCode == subdistrictCode
	case "BLOCK":
		// In our schema subdistrict_code == block-level grouping
		return targetCode == subdistrictCode
	case "VILLAGE":
		h.DB.QueryRow(context.Background(),
			`SELECT EXISTS(SELECT 1 FROM lgd_hierarchy WHERE subdistrict_code = $1 AND village_code = $2 LIMIT 1)`,
			subdistrictCode, targetCode).Scan(&exists)
		return exists
	default:
		return false
	}
}

// isVillageInBlock checks if a village belongs to a specific block.
func (h *PollHandler) isVillageInBlock(villageCode, blockCode int) bool {
	var exists bool
	h.DB.QueryRow(context.Background(),
		`SELECT EXISTS(SELECT 1 FROM lgd_hierarchy WHERE subdistrict_code = $1 AND village_code = $2 LIMIT 1)`,
		blockCode, villageCode).Scan(&exists)
	return exists
}

// verifyLgdTarget checks that the target LGD code exists.
func (h *PollHandler) verifyLgdTarget(targetLevel string, targetCode int) bool {
	var exists bool
	var query string

	switch targetLevel {
	case "DISTRICT":
		query = `SELECT EXISTS(SELECT 1 FROM lgd_hierarchy WHERE district_code = $1 LIMIT 1)`
	case "SUBDIVISION", "BLOCK":
		query = `SELECT EXISTS(SELECT 1 FROM lgd_hierarchy WHERE subdistrict_code = $1 LIMIT 1)`
	case "VILLAGE":
		query = `SELECT EXISTS(SELECT 1 FROM lgd_hierarchy WHERE village_code = $1 LIMIT 1)`
	default:
		return false
	}

	h.DB.QueryRow(context.Background(), query, targetCode).Scan(&exists)
	return exists
}

// ═══════════════════════════════════════════════════════════════
// CITIZEN: GetActivePolls — Hierarchy-matched polls for citizen
// GET /api/v1/polls
// ═══════════════════════════════════════════════════════════════
func (h *PollHandler) GetActivePolls(c *gin.Context) {
	userID, _ := c.Get("userID")
	userIDStr := fmt.Sprintf("%v", userID)

	// Get citizen's LGD hierarchy codes
	var districtCode, subdistrictCode, villageCode *int
	h.DB.QueryRow(context.Background(),
		`SELECT district_code, subdistrict_code, village_code FROM users WHERE id = $1`,
		userIDStr).Scan(&districtCode, &subdistrictCode, &villageCode)

	if districtCode == nil {
		c.JSON(http.StatusOK, models.PollListResponse{Polls: []models.CustomPoll{}, Total: 0})
		return
	}

	// Fetch all active polls that target the citizen's area.
	// A poll matches if:
	//   - target_level='DISTRICT'    AND target_code = citizen's district_code
	//   - target_level='SUBDIVISION' AND target_code = citizen's subdistrict_code
	//   - target_level='BLOCK'       AND target_code = citizen's subdistrict_code (block==subdistrict in our schema)
	//   - target_level='VILLAGE'     AND target_code = citizen's village_code
	rows, err := h.DB.Query(context.Background(),
		`SELECT cp.poll_id, cp.title, cp.description, cp.target_level, cp.target_code,
		        cp.options, cp.created_at::text
		 FROM custom_polls cp
		 WHERE cp.is_active = TRUE
		   AND (cp.expires_at IS NULL OR cp.expires_at > NOW())
		   AND (
		     (cp.target_level = 'DISTRICT'    AND cp.target_code = $1)
		     OR (cp.target_level = 'SUBDIVISION' AND cp.target_code = $2)
		     OR (cp.target_level = 'BLOCK'       AND cp.target_code = $2)
		     OR (cp.target_level = 'VILLAGE'     AND cp.target_code = $3)
		   )
		   AND NOT EXISTS (
		     SELECT 1 FROM custom_poll_responses cpr
		     WHERE cpr.poll_id = cp.poll_id AND cpr.user_id = $4
		   )
		 ORDER BY cp.created_at DESC`,
		*districtCode, safeInt(subdistrictCode), safeInt(villageCode), userIDStr)

	if err != nil {
		fmt.Printf("GetActivePolls error: %v\n", err)
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to fetch polls",
		})
		return
	}
	defer rows.Close()

	var polls []models.CustomPoll
	for rows.Next() {
		var p models.CustomPoll
		var optionsJSON []byte
		rows.Scan(&p.PollID, &p.Title, &p.Description, &p.TargetLevel, &p.TargetCode,
			&optionsJSON, &p.CreatedAt)
		json.Unmarshal(optionsJSON, &p.Options)
		p.IsActive = true
		polls = append(polls, p)
	}

	if polls == nil {
		polls = []models.CustomPoll{}
	}

	c.JSON(http.StatusOK, models.PollListResponse{Polls: polls, Total: len(polls)})
}

// ═══════════════════════════════════════════════════════════════
// CITIZEN: SubmitPoll — Hardened response collection
// POST /api/v1/polls/submit
//
// Senior-grade validations:
//  1. Poll existence + active status + expiry check
//  2. Option index bounds validation
//  3. Duplicate vote detection → 409 ALREADY_VOTED
//  4. LGD eligibility — citizen must be in the poll's target area
//  5. Optional PostGIS GPS geofence
//  6. Atomic insert with conflict rejection
// ═══════════════════════════════════════════════════════════════
func (h *PollHandler) SubmitPoll(c *gin.Context) {
	var req models.PollSubmitRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_INPUT",
			Message: "poll_id and selected_option_index are required",
		})
		return
	}

	userID, _ := c.Get("userID")
	userIDStr := fmt.Sprintf("%v", userID)

	// ─── Step 1: Fetch poll + validate active + expiry ──────
	var optionsJSON []byte
	var isActive bool
	var targetLevel string
	var targetCode int
	var expiresAt *string
	err := h.DB.QueryRow(context.Background(),
		`SELECT options, is_active, target_level, target_code,
		        CASE WHEN expires_at IS NOT NULL THEN expires_at::text ELSE NULL END
		 FROM custom_polls WHERE poll_id = $1`,
		req.PollID).Scan(&optionsJSON, &isActive, &targetLevel, &targetCode, &expiresAt)

	if err != nil {
		c.JSON(http.StatusNotFound, models.ErrorResponse{
			Error:   "POLL_NOT_FOUND",
			Message: "This poll does not exist.",
		})
		return
	}
	if !isActive {
		c.JSON(http.StatusGone, models.ErrorResponse{
			Error:   "POLL_CLOSED",
			Message: "This poll is no longer active.",
		})
		return
	}

	// Check expiry server-side
	if expiresAt != nil {
		var expired bool
		h.DB.QueryRow(context.Background(),
			`SELECT $1::timestamptz < NOW()`, *expiresAt).Scan(&expired)
		if expired {
			c.JSON(http.StatusGone, models.ErrorResponse{
				Error:   "POLL_EXPIRED",
				Message: "This poll has expired.",
			})
			return
		}
	}

	// ─── Step 2: Parse options + validate index ─────────────
	var options []string
	json.Unmarshal(optionsJSON, &options)

	if req.SelectedOptionIndex < 0 || req.SelectedOptionIndex >= len(options) {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_OPTION",
			Message: fmt.Sprintf("Option index %d is invalid. Poll has %d options (0-%d).", req.SelectedOptionIndex, len(options), len(options)-1),
		})
		return
	}
	selectedText := options[req.SelectedOptionIndex]

	// ─── Step 3: Duplicate vote detection ───────────────────
	// Check if citizen already voted on this poll
	var existingResponseID *string
	var existingOptionText *string
	h.DB.QueryRow(context.Background(),
		`SELECT response_id::text, selected_option_text
		 FROM custom_poll_responses
		 WHERE poll_id = $1 AND user_id = $2`,
		req.PollID, userIDStr).Scan(&existingResponseID, &existingOptionText)

	if existingResponseID != nil {
		c.JSON(http.StatusConflict, models.ErrorResponse{
			Error:   "ALREADY_VOTED",
			Message: fmt.Sprintf("You have already voted '%s' on this poll. Duplicate voting is not allowed.", *existingOptionText),
		})
		return
	}

	// ─── Step 4: LGD eligibility — citizen in target area ───
	var citizenDistrictCode, citizenSubdistrictCode, citizenVillageCode *int
	h.DB.QueryRow(context.Background(),
		`SELECT district_code, subdistrict_code, village_code FROM users WHERE id = $1`,
		userIDStr).Scan(&citizenDistrictCode, &citizenSubdistrictCode, &citizenVillageCode)

	eligible := false
	switch targetLevel {
	case "DISTRICT":
		eligible = citizenDistrictCode != nil && *citizenDistrictCode == targetCode
	case "SUBDIVISION", "BLOCK":
		eligible = citizenSubdistrictCode != nil && *citizenSubdistrictCode == targetCode
	case "VILLAGE":
		eligible = citizenVillageCode != nil && *citizenVillageCode == targetCode
	}

	if !eligible {
		c.JSON(http.StatusForbidden, models.ErrorResponse{
			Error:   "NOT_IN_TARGET_AREA",
			Message: "This poll is not targeted at your area. You are not eligible to vote.",
		})
		return
	}

	// ─── Step 5: Optional GPS geofence (PostGIS) ────────────
	var distanceMeters float64
	if req.GpsLat != 0 && req.GpsLng != 0 {
		var fpsID *string
		h.DB.QueryRow(context.Background(),
			`SELECT fps_id FROM users WHERE id = $1`, userIDStr).Scan(&fpsID)

		if fpsID != nil {
			err := h.DB.QueryRow(context.Background(),
				`SELECT ST_DistanceSphere(ST_MakePoint($1, $2), location)
				 FROM fair_price_shops WHERE fps_id = $3`,
				req.GpsLng, req.GpsLat, *fpsID).Scan(&distanceMeters)

			if err == nil && distanceMeters > 100 {
				c.JSON(http.StatusForbidden, models.ErrorResponse{
					Error:   "GEO_FENCE_VIOLATION",
					Message: "You must be physically present at the Fair Price Shop.",
				})
				return
			}
		}
	}

	// ─── Step 6: Atomic insert (no upsert — reject conflicts) ─
	_, err = h.DB.Exec(context.Background(),
		`INSERT INTO custom_poll_responses
		   (poll_id, user_id, selected_option_index, selected_option_text,
		    gps_lat, gps_lng, distance_from_shop_meters)
		 VALUES ($1, $2, $3, $4, $5, $6, $7)`,
		req.PollID, userIDStr, req.SelectedOptionIndex, selectedText,
		req.GpsLat, req.GpsLng, distanceMeters)

	if err != nil {
		// Catch unique constraint violation (race condition fallback)
		if isPgUniqueViolation(err) {
			c.JSON(http.StatusConflict, models.ErrorResponse{
				Error:   "ALREADY_VOTED",
				Message: "Duplicate vote detected. You have already voted on this poll.",
			})
			return
		}
		fmt.Printf("Poll response insert error: %v\\n", err)
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to record your response. Please try again.",
		})
		return
	}

	c.JSON(http.StatusCreated, models.PollSubmitResponse{
		Success:  true,
		Distance: distanceMeters,
		Message:  fmt.Sprintf("Vote recorded: '%s'", selectedText),
	})
}

// ═══════════════════════════════════════════════════════════════
// ADMIN: GetPollAnalytics — Results breakdown per option
// GET /api/v1/admin/polls
// ═══════════════════════════════════════════════════════════════
func (h *PollHandler) GetPollAnalytics(c *gin.Context) {
	officerRole, _ := c.Get("officerRole")
	role := fmt.Sprintf("%v", officerRole)

	// Build jurisdiction filter
	var whereClause string
	var args []interface{}

	switch role {
	case models.RoleAdminState:
		whereClause = "1=1"
	case models.RoleAdminDistrict:
		callerDistrict, _ := c.Get("officerDistrictCode")
		whereClause = "cp.target_code IN (SELECT DISTINCT district_code FROM lgd_hierarchy WHERE district_code = $1) OR cp.target_code IN (SELECT DISTINCT subdistrict_code FROM lgd_hierarchy WHERE district_code = $1) OR cp.target_code IN (SELECT DISTINCT village_code FROM lgd_hierarchy WHERE district_code = $1)"
		args = append(args, callerDistrict)
	case models.RoleAdminSubdivision:
		callerSubdistrict, _ := c.Get("officerSubdistrictCode")
		whereClause = "cp.target_code = $1 OR cp.target_code IN (SELECT DISTINCT village_code FROM lgd_hierarchy WHERE subdistrict_code = $1)"
		args = append(args, callerSubdistrict)
	case models.RoleAdminBlock:
		callerBlock, _ := c.Get("officerBlockCode")
		whereClause = "cp.target_code IN (SELECT DISTINCT village_code FROM lgd_hierarchy WHERE subdistrict_code = $1)"
		args = append(args, callerBlock)
	default:
		c.JSON(http.StatusForbidden, models.ErrorResponse{Error: "FORBIDDEN", Message: "Not authorized"})
		return
	}

	query := fmt.Sprintf(`
		SELECT cp.poll_id, cp.title, cp.target_level, cp.target_code,
		       cp.options, cp.is_active, cp.created_at::text,
		       COUNT(cpr.response_id) AS total_responses
		FROM custom_polls cp
		LEFT JOIN custom_poll_responses cpr ON cp.poll_id = cpr.poll_id
		WHERE %s
		GROUP BY cp.poll_id
		ORDER BY cp.created_at DESC`, whereClause)

	rows, err := h.DB.Query(context.Background(), query, args...)
	if err != nil {
		fmt.Printf("GetPollAnalytics error: %v\n", err)
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error: "DB_ERROR", Message: "Failed to fetch poll analytics",
		})
		return
	}
	defer rows.Close()

	var polls []models.CustomPoll
	for rows.Next() {
		var p models.CustomPoll
		var optionsJSON []byte
		rows.Scan(&p.PollID, &p.Title, &p.TargetLevel, &p.TargetCode,
			&optionsJSON, &p.IsActive, &p.CreatedAt, &p.TotalResponses)
		json.Unmarshal(optionsJSON, &p.Options)
		polls = append(polls, p)
	}

	// Fetch per-option breakdown for each poll
	for i := range polls {
		polls[i].OptionCounts = make(map[string]int)
		breakdownRows, err := h.DB.Query(context.Background(),
			`SELECT selected_option_text, COUNT(*) FROM custom_poll_responses
			 WHERE poll_id = $1 GROUP BY selected_option_text`,
			polls[i].PollID)
		if err == nil {
			for breakdownRows.Next() {
				var text string
				var count int
				breakdownRows.Scan(&text, &count)
				polls[i].OptionCounts[text] = count
			}
			breakdownRows.Close()
		}
	}

	if polls == nil {
		polls = []models.CustomPoll{}
	}

	c.JSON(http.StatusOK, models.PollListResponse{Polls: polls, Total: len(polls)})
}

// ═══════════════════════════════════════════════════════════════
// ADMIN: ClosePoll — Deactivate a poll
// PATCH /api/v1/admin/poll/:id/close
// ═══════════════════════════════════════════════════════════════
func (h *PollHandler) ClosePoll(c *gin.Context) {
	pollID := c.Param("id")
	officerID, _ := c.Get("officerID")

	result, err := h.DB.Exec(context.Background(),
		`UPDATE custom_polls SET is_active = FALSE, updated_at = NOW()
		 WHERE poll_id = $1 AND created_by = $2`,
		pollID, fmt.Sprintf("%v", officerID))

	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error: "DB_ERROR", Message: "Failed to close poll",
		})
		return
	}

	if result.RowsAffected() == 0 {
		c.JSON(http.StatusNotFound, models.ErrorResponse{
			Error: "NOT_FOUND", Message: "Poll not found or you don't have permission to close it",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{"success": true, "message": "Poll closed"})
}

// safeInt dereferences an *int, returning 0 if nil.
func safeInt(v *int) int {
	if v == nil {
		return 0
	}
	return *v
}

// isPgUniqueViolation checks if a pgx error is a unique constraint violation (23505).
func isPgUniqueViolation(err error) bool {
	var pgErr *pgconn.PgError
	if errors.As(err, &pgErr) {
		return pgErr.Code == "23505"
	}
	return false
}

// ═══════════════════════════════════════════════════════════════
// PER-USER RATE LIMITER — 1 submission per user per 60 seconds
// Prevents brute-force voting attempts (AGENTS.md requirement)
// ═══════════════════════════════════════════════════════════════
type rateLimiter struct {
	mu       sync.Mutex
	lastVote map[string]time.Time
}

var pollRateLimiter = &rateLimiter{
	lastVote: make(map[string]time.Time),
}

// CheckRateLimit returns true if the user can submit (not rate-limited).
func (rl *rateLimiter) CheckRateLimit(userID string) bool {
	rl.mu.Lock()
	defer rl.mu.Unlock()

	last, exists := rl.lastVote[userID]
	if exists && time.Since(last) < 60*time.Second {
		return false
	}
	rl.lastVote[userID] = time.Now()
	return true
}

// PollRateLimitMiddleware enforces per-user poll submission rate limiting.
// Must be applied AFTER SupabaseAuthMiddleware which sets "userID" in context.
func PollRateLimitMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		userID, exists := c.Get("userID")
		if !exists {
			c.JSON(http.StatusUnauthorized, models.ErrorResponse{
				Error:   "UNAUTHORIZED",
				Message: "Authentication required.",
			})
			c.Abort()
			return
		}

		if !pollRateLimiter.CheckRateLimit(fmt.Sprintf("%v", userID)) {
			c.JSON(http.StatusTooManyRequests, models.ErrorResponse{
				Error:   "RATE_LIMITED",
				Message: "You can only submit one vote per minute. Please wait before trying again.",
			})
			c.Abort()
			return
		}

		c.Next()
	}
}

// ═══════════════════════════════════════════════════════════════
// CITIZEN: GetMyResponses — Past votes for the authenticated user
// GET /api/v1/polls/my-responses
//
// Returns all polls the citizen has voted on with their selection,
// timestamp, and distance. Used by the app to show "Already Voted"
// badges and voting history.
// ═══════════════════════════════════════════════════════════════
func (h *PollHandler) GetMyResponses(c *gin.Context) {
	userID, _ := c.Get("userID")
	userIDStr := fmt.Sprintf("%v", userID)

	rows, err := h.DB.Query(context.Background(),
		`SELECT r.response_id, r.poll_id, r.selected_option_index, r.selected_option_text,
		        r.gps_lat, r.gps_lng, r.distance_from_shop_meters, r.submitted_at,
		        p.title, p.is_active
		 FROM custom_poll_responses r
		 JOIN custom_polls p ON r.poll_id = p.poll_id
		 WHERE r.user_id = $1
		 ORDER BY r.submitted_at DESC
		 LIMIT 100`, userIDStr)

	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to fetch your responses.",
		})
		return
	}
	defer rows.Close()

	var responses []models.MyPollResponse
	for rows.Next() {
		var r models.MyPollResponse
		rows.Scan(
			&r.ResponseID, &r.PollID, &r.SelectedOptionIndex, &r.SelectedOptionText,
			&r.GpsLat, &r.GpsLng, &r.DistanceMeters, &r.SubmittedAt,
			&r.PollTitle, &r.PollActive,
		)
		responses = append(responses, r)
	}

	if responses == nil {
		responses = []models.MyPollResponse{}
	}

	c.JSON(http.StatusOK, models.MyResponsesListResponse{
		Responses: responses,
		Total:     len(responses),
	})
}
