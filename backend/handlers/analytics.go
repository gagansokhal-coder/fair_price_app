package handlers

import (
	"context"
	"encoding/json"
	"fmt"
	"math"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
	"pds-backend/models"
)

// ═══════════════════════════════════════════════════════════════
// ANALYTICS ENGINE — Phases 5-8
//
// Provides hierarchical analytics with:
//  - Per-option aggregation at village/block/subdivision/district
//  - Zone classification (GREEN/YELLOW/RED)
//  - Pie chart + bar chart + zone map API responses
//  - Strict RBAC: officers see ONLY their jurisdiction
// ═══════════════════════════════════════════════════════════════

// AnalyticsHandler provides analytics API endpoints.
type AnalyticsHandler struct {
	DB *pgxpool.Pool
}

// NewAnalyticsHandler creates a new Analytics handler.
func NewAnalyticsHandler(db *pgxpool.Pool) *AnalyticsHandler {
	return &AnalyticsHandler{DB: db}
}

// Zone thresholds
const (
	ZoneGreenThreshold  = 70.0 // >70% positive = GREEN
	ZoneYellowThreshold = 40.0 // 40-70% = YELLOW, <40% = RED
)

// classifyZone returns GREEN/YELLOW/RED based on positive response percentage.
func classifyZone(positivePct float64) string {
	if positivePct >= ZoneGreenThreshold {
		return "GREEN"
	} else if positivePct >= ZoneYellowThreshold {
		return "YELLOW"
	}
	return "RED"
}

// ═══════════════════════════════════════════════════════════════
// GET /api/v1/admin/analytics/summary
//
// Returns a jurisdiction-scoped overview:
//  - Total polls, total responses
//  - Zone distribution counts
//  - List of zones with classification
//
// RBAC enforced: officer sees only their area.
// ═══════════════════════════════════════════════════════════════
func (h *AnalyticsHandler) GetAnalyticsSummary(c *gin.Context) {
	role, jurisdictionFilter, args := h.buildJurisdictionFilter(c)
	if role == "" {
		return // error already sent
	}

	// 1. Aggregate responses per area (village/block/subdistrict)
	//    For each custom_poll, we group responses by the citizen's LGD location
	query := fmt.Sprintf(`
		SELECT
			lh.subdistrict_code AS area_code,
			lh.subdistrict_name AS area_name,
			'BLOCK' AS area_level,
			lh.district_code,
			lh.district_name,
			COUNT(cpr.response_id) AS total_responses,
			COUNT(CASE WHEN cpr.selected_option_index = 0 THEN 1 END) AS positive_count,
			COUNT(DISTINCT cp.poll_id) AS polls_count
		FROM custom_poll_responses cpr
		JOIN custom_polls cp ON cpr.poll_id = cp.poll_id
		JOIN users u ON cpr.user_id = u.id
		JOIN lgd_hierarchy lh ON u.subdistrict_code = lh.subdistrict_code
		WHERE %s
		GROUP BY lh.subdistrict_code, lh.subdistrict_name, lh.district_code, lh.district_name
		ORDER BY total_responses DESC
	`, jurisdictionFilter)

	rows, err := h.DB.Query(context.Background(), query, args...)
	if err != nil {
		fmt.Printf("Analytics summary error: %v\n", err)
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error: "DB_ERROR", Message: "Failed to compute analytics",
		})
		return
	}
	defer rows.Close()

	var zones []models.ZoneEntry
	var totalResponses, totalPositive int
	greenCount, yellowCount, redCount := 0, 0, 0

	for rows.Next() {
		var z models.ZoneEntry
		var districtCode int
		var districtName string
		var positiveCount, pollsCount int

		rows.Scan(&z.AreaCode, &z.AreaName, &z.AreaLevel,
			&districtCode, &districtName,
			&z.TotalResponses, &positiveCount, &pollsCount)

		z.PositiveCount = positiveCount
		if z.TotalResponses > 0 {
			z.PositivePct = math.Round(float64(positiveCount)/float64(z.TotalResponses)*1000) / 10
		}
		z.Zone = classifyZone(z.PositivePct)
		z.DistrictName = districtName

		totalResponses += z.TotalResponses
		totalPositive += positiveCount

		switch z.Zone {
		case "GREEN":
			greenCount++
		case "YELLOW":
			yellowCount++
		case "RED":
			redCount++
		}

		zones = append(zones, z)
	}

	if zones == nil {
		zones = []models.ZoneEntry{}
	}

	avgPositivePct := 0.0
	if totalResponses > 0 {
		avgPositivePct = math.Round(float64(totalPositive)/float64(totalResponses)*1000) / 10
	}

	c.JSON(http.StatusOK, models.AnalyticsSummaryResponse{
		TotalResponses: totalResponses,
		TotalZones:     len(zones),
		GreenZones:     greenCount,
		YellowZones:    yellowCount,
		RedZones:       redCount,
		AvgPositivePct: avgPositivePct,
		Zones:          zones,
	})
}

// ═══════════════════════════════════════════════════════════════
// GET /api/v1/admin/analytics/poll/:id
//
// Returns detailed analytics for a single poll:
//  - Pie chart data (option-wise percentage distribution)
//  - Bar chart data (per-area comparison)
//  - Zone map (village/block tagged GREEN/YELLOW/RED)
//
// RBAC enforced: officer must have jurisdiction over the poll target.
// ═══════════════════════════════════════════════════════════════
func (h *AnalyticsHandler) GetPollAnalyticsDetail(c *gin.Context) {
	pollID := c.Param("id")
	role, _, _ := h.buildJurisdictionFilter(c)
	if role == "" {
		return
	}

	// 1. Fetch poll info
	var poll models.CustomPoll
	var optionsJSON []byte
	err := h.DB.QueryRow(context.Background(),
		`SELECT poll_id, title, description, target_level, target_code, options, is_active, created_at::text
		 FROM custom_polls WHERE poll_id = $1`, pollID,
	).Scan(&poll.PollID, &poll.Title, &poll.Description, &poll.TargetLevel,
		&poll.TargetCode, &optionsJSON, &poll.IsActive, &poll.CreatedAt)

	if err != nil {
		c.JSON(http.StatusNotFound, models.ErrorResponse{
			Error: "POLL_NOT_FOUND", Message: "Poll not found",
		})
		return
	}
	json.Unmarshal(optionsJSON, &poll.Options)

	// RBAC: verify officer has access to this poll's target
	if !h.officerCanAccessPoll(c, poll.TargetLevel, poll.TargetCode) {
		c.JSON(http.StatusForbidden, models.ErrorResponse{
			Error: "FORBIDDEN", Message: "You don't have jurisdiction over this poll's target area",
		})
		return
	}

	// 2. Pie chart — option-wise counts for the entire poll
	pieRows, err := h.DB.Query(context.Background(),
		`SELECT selected_option_text, COUNT(*)
		 FROM custom_poll_responses WHERE poll_id = $1
		 GROUP BY selected_option_text
		 ORDER BY selected_option_index`, pollID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error: "DB_ERROR", Message: "Failed to fetch pie chart data",
		})
		return
	}

	var pieLabels []string
	var pieValues []int
	var piePcts []float64
	var totalVotes int

	type optRow struct {
		label string
		count int
	}
	var tempOpts []optRow
	for pieRows.Next() {
		var label string
		var count int
		pieRows.Scan(&label, &count)
		tempOpts = append(tempOpts, optRow{label, count})
		totalVotes += count
	}
	pieRows.Close()

	for _, o := range tempOpts {
		pieLabels = append(pieLabels, o.label)
		pieValues = append(pieValues, o.count)
		pct := 0.0
		if totalVotes > 0 {
			pct = math.Round(float64(o.count)/float64(totalVotes)*1000) / 10
		}
		piePcts = append(piePcts, pct)
	}

	pieChart := models.PieChartData{
		Labels:      pieLabels,
		Values:      pieValues,
		Percentages: piePcts,
	}

	// 3. Bar chart — per-area (village/block) breakdown of each option
	barQuery := `
		SELECT
			COALESCE(lh.subdistrict_name, 'Unknown') AS area_name,
			cpr.selected_option_text,
			COUNT(*) AS cnt
		FROM custom_poll_responses cpr
		JOIN users u ON cpr.user_id = u.id
		LEFT JOIN lgd_hierarchy lh ON u.subdistrict_code = lh.subdistrict_code
		WHERE cpr.poll_id = $1
		GROUP BY area_name, cpr.selected_option_text, cpr.selected_option_index
		ORDER BY area_name, cpr.selected_option_index
	`
	barRows, err := h.DB.Query(context.Background(), barQuery, pollID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error: "DB_ERROR", Message: "Failed to fetch bar chart data",
		})
		return
	}

	// Build area → option → count map
	areaOrder := []string{}
	areaSet := map[string]bool{}
	areaOptionMap := map[string]map[string]int{} // area -> option -> count

	for barRows.Next() {
		var area, option string
		var cnt int
		barRows.Scan(&area, &option, &cnt)

		if !areaSet[area] {
			areaSet[area] = true
			areaOrder = append(areaOrder, area)
		}
		if areaOptionMap[area] == nil {
			areaOptionMap[area] = map[string]int{}
		}
		areaOptionMap[area][option] = cnt
	}
	barRows.Close()

	// Build series: one per option
	var barSeries []models.BarChartSeries
	for _, opt := range poll.Options {
		series := models.BarChartSeries{Label: opt, Values: []int{}}
		for _, area := range areaOrder {
			series.Values = append(series.Values, areaOptionMap[area][opt])
		}
		barSeries = append(barSeries, series)
	}

	barChart := models.BarChartData{
		Categories: areaOrder,
		Series:     barSeries,
	}

	// 4. Zone map — per-area zone classification based on positive (index 0) pct
	zoneQuery := `
		SELECT
			COALESCE(lh.subdistrict_code, 0) AS area_code,
			COALESCE(lh.subdistrict_name, 'Unknown') AS area_name,
			COUNT(*) AS total,
			COUNT(CASE WHEN cpr.selected_option_index = 0 THEN 1 END) AS positive
		FROM custom_poll_responses cpr
		JOIN users u ON cpr.user_id = u.id
		LEFT JOIN lgd_hierarchy lh ON u.subdistrict_code = lh.subdistrict_code
		WHERE cpr.poll_id = $1
		GROUP BY lh.subdistrict_code, lh.subdistrict_name
		ORDER BY total DESC
	`
	zoneRows, err := h.DB.Query(context.Background(), zoneQuery, pollID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error: "DB_ERROR", Message: "Failed to fetch zone data",
		})
		return
	}

	var zoneMap []models.ZoneEntry
	for zoneRows.Next() {
		var z models.ZoneEntry
		var positive int
		zoneRows.Scan(&z.AreaCode, &z.AreaName, &z.TotalResponses, &positive)
		z.AreaLevel = "BLOCK"
		z.PositiveCount = positive
		if z.TotalResponses > 0 {
			z.PositivePct = math.Round(float64(positive)/float64(z.TotalResponses)*1000) / 10
		}
		z.Zone = classifyZone(z.PositivePct)
		zoneMap = append(zoneMap, z)
	}
	zoneRows.Close()

	if zoneMap == nil {
		zoneMap = []models.ZoneEntry{}
	}

	c.JSON(http.StatusOK, models.PollAnalyticsDetailResponse{
		PollID:         poll.PollID,
		Title:          poll.Title,
		TargetLevel:    poll.TargetLevel,
		TargetCode:     poll.TargetCode,
		Options:        poll.Options,
		IsActive:       poll.IsActive,
		TotalResponses: totalVotes,
		PieChart:       pieChart,
		BarChart:       barChart,
		ZoneMap:        zoneMap,
	})
}

// ═══════════════════════════════════════════════════════════════
// RBAC HELPERS — Strict jurisdiction enforcement
// ═══════════════════════════════════════════════════════════════

// buildJurisdictionFilter returns a SQL WHERE clause + args scoped to the
// authenticated officer's jurisdiction. This is the core RBAC enforcement.
func (h *AnalyticsHandler) buildJurisdictionFilter(c *gin.Context) (string, string, []interface{}) {
	officerRole, exists := c.Get("officerRole")
	if !exists {
		c.JSON(http.StatusUnauthorized, models.ErrorResponse{
			Error: "UNAUTHORIZED", Message: "Authentication required",
		})
		return "", "", nil
	}
	role := fmt.Sprintf("%v", officerRole)

	var filter string
	var args []interface{}

	switch role {
	case models.RoleAdminState:
		filter = "1=1" // Full access

	case models.RoleAdminDistrict:
		districtCode, _ := c.Get("officerDistrictCode")
		args = append(args, districtCode)
		filter = fmt.Sprintf("lh.district_code = $%d", len(args))

	case models.RoleAdminSubdivision:
		subdistrictCode, _ := c.Get("officerSubdistrictCode")
		args = append(args, subdistrictCode)
		filter = fmt.Sprintf("lh.subdistrict_code = $%d", len(args))

	case models.RoleAdminBlock:
		subdistrictCode, _ := c.Get("officerSubdistrictCode")
		args = append(args, subdistrictCode)
		filter = fmt.Sprintf("lh.subdistrict_code = $%d", len(args))

	default:
		c.JSON(http.StatusForbidden, models.ErrorResponse{
			Error: "FORBIDDEN", Message: "Citizens cannot access analytics",
		})
		return "", "", nil
	}

	return role, filter, args
}

// officerCanAccessPoll checks if the officer has jurisdiction over a poll's target.
func (h *AnalyticsHandler) officerCanAccessPoll(c *gin.Context, targetLevel string, targetCode int) bool {
	officerRole, _ := c.Get("officerRole")
	role := fmt.Sprintf("%v", officerRole)

	switch role {
	case models.RoleAdminState:
		return true // Always

	case models.RoleAdminDistrict:
		districtCode, _ := c.Get("officerDistrictCode")
		if targetLevel == "DISTRICT" {
			return districtCode == targetCode
		}
		// Check if target area is under this district
		var count int
		h.DB.QueryRow(context.Background(),
			`SELECT COUNT(*) FROM lgd_hierarchy
			 WHERE district_code = $1 AND subdistrict_code = $2`,
			districtCode, targetCode).Scan(&count)
		return count > 0

	case models.RoleAdminSubdivision, models.RoleAdminBlock:
		subdistrictCode, _ := c.Get("officerSubdistrictCode")
		if targetLevel == "SUBDIVISION" || targetLevel == "BLOCK" {
			return subdistrictCode == targetCode
		}
		// Check if village is under this block
		if targetLevel == "VILLAGE" {
			var count int
			h.DB.QueryRow(context.Background(),
				`SELECT COUNT(*) FROM lgd_hierarchy
				 WHERE subdistrict_code = $1 AND village_code = $2`,
				subdistrictCode, targetCode).Scan(&count)
			return count > 0
		}
		return false

	default:
		return false
	}
}

// ═══════════════════════════════════════════════════════════════
// GET /api/v1/admin/analytics/zones
//
// Returns village-level zone classification across all polls
// in the officer's jurisdiction. Each village/block is tagged
// GREEN/YELLOW/RED based on average positive response rate.
// ═══════════════════════════════════════════════════════════════
func (h *AnalyticsHandler) GetZoneClassification(c *gin.Context) {
	role, jurisdictionFilter, args := h.buildJurisdictionFilter(c)
	if role == "" {
		return
	}

	// Village-level granular zones
	query := fmt.Sprintf(`
		SELECT
			lh.village_code AS area_code,
			lh.village_name AS area_name,
			'VILLAGE' AS area_level,
			lh.subdistrict_name AS parent_name,
			lh.district_name,
			COUNT(cpr.response_id) AS total_responses,
			COUNT(CASE WHEN cpr.selected_option_index = 0 THEN 1 END) AS positive_count
		FROM custom_poll_responses cpr
		JOIN custom_polls cp ON cpr.poll_id = cp.poll_id
		JOIN users u ON cpr.user_id = u.id
		JOIN lgd_hierarchy lh ON u.village_code = lh.village_code
		WHERE %s
		GROUP BY lh.village_code, lh.village_name, lh.subdistrict_name, lh.district_name
		HAVING COUNT(cpr.response_id) > 0
		ORDER BY COUNT(cpr.response_id) DESC
	`, jurisdictionFilter)

	rows, err := h.DB.Query(context.Background(), query, args...)
	if err != nil {
		fmt.Printf("Zone classification error: %v\n", err)
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error: "DB_ERROR", Message: "Failed to compute zone classification",
		})
		return
	}
	defer rows.Close()

	var zones []models.ZoneEntry
	greenCount, yellowCount, redCount := 0, 0, 0

	for rows.Next() {
		var z models.ZoneEntry
		var parentName, districtName string
		var positive int

		rows.Scan(&z.AreaCode, &z.AreaName, &z.AreaLevel,
			&parentName, &districtName,
			&z.TotalResponses, &positive)

		z.PositiveCount = positive
		z.DistrictName = districtName
		z.ParentName = parentName
		if z.TotalResponses > 0 {
			z.PositivePct = math.Round(float64(positive)/float64(z.TotalResponses)*1000) / 10
		}
		z.Zone = classifyZone(z.PositivePct)

		switch z.Zone {
		case "GREEN":
			greenCount++
		case "YELLOW":
			yellowCount++
		case "RED":
			redCount++
		}

		zones = append(zones, z)
	}

	if zones == nil {
		zones = []models.ZoneEntry{}
	}

	c.JSON(http.StatusOK, models.ZoneClassificationResponse{
		TotalZones:  len(zones),
		GreenZones:  greenCount,
		YellowZones: yellowCount,
		RedZones:    redCount,
		Zones:       zones,
	})
}
