package handlers

import (
	"context"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
	"pds-backend/models"
)

// OfficerHandler handles officer CRUD operations.
type OfficerHandler struct {
	DB *pgxpool.Pool
}

// NewOfficerHandler creates a new Officer handler.
func NewOfficerHandler(db *pgxpool.Pool) *OfficerHandler {
	return &OfficerHandler{DB: db}
}

// CreateOfficer creates a new officer with RBAC + LGD validation.
// POST /api/v1/admin/create-officer
func (h *OfficerHandler) CreateOfficer(c *gin.Context) {
	var req models.CreateOfficerRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_INPUT",
			Message: "Name, phone (10 digits), and role are required",
		})
		return
	}

	// Validate role is a known role
	if _, ok := models.RoleHierarchy[req.Role]; !ok {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_ROLE",
			Message: "Role must be one of: ADMIN_STATE, ADMIN_DISTRICT, ADMIN_SUBDIVISION, ADMIN_BLOCK",
		})
		return
	}

	// Cannot create CITIZEN via this endpoint
	if req.Role == models.RoleCitizen {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_ROLE",
			Message: "Citizens register themselves. Use this endpoint for officer roles only.",
		})
		return
	}

	// RBAC: Check if the caller can create this role
	callerRole, _ := c.Get("officerRole")
	if !CanCreateRole(fmt.Sprintf("%v", callerRole), req.Role) {
		c.JSON(http.StatusForbidden, models.ErrorResponse{
			Error:   "RBAC_VIOLATION",
			Message: fmt.Sprintf("Your role (%s) cannot create a %s officer", callerRole, req.Role),
		})
		return
	}

	// Validate required LGD codes based on target role
	if req.Role == models.RoleAdminDistrict && req.DistrictCode == nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "MISSING_LGD",
			Message: "District code is required for ADMIN_DISTRICT role",
		})
		return
	}
	if req.Role == models.RoleAdminSubdivision && (req.DistrictCode == nil || req.SubdistrictCode == nil) {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "MISSING_LGD",
			Message: "District and subdistrict codes are required for ADMIN_SUBDIVISION role",
		})
		return
	}
	if req.Role == models.RoleAdminBlock && (req.DistrictCode == nil || req.BlockCode == nil) {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "MISSING_LGD",
			Message: "District and block codes are required for ADMIN_BLOCK role",
		})
		return
	}

	// Jurisdiction check: caller can only create within their area
	if !IsWithinJurisdiction(c, req.DistrictCode, req.BlockCode) {
		c.JSON(http.StatusForbidden, models.ErrorResponse{
			Error:   "JURISDICTION_VIOLATION",
			Message: "You can only create officers within your assigned LGD area",
		})
		return
	}

	// Validate LGD codes exist in hierarchy
	if req.DistrictCode != nil {
		var exists bool
		h.DB.QueryRow(context.Background(),
			`SELECT EXISTS(SELECT 1 FROM lgd_hierarchy WHERE district_code = $1 AND state_code = 3)`,
			*req.DistrictCode).Scan(&exists)
		if !exists {
			c.JSON(http.StatusBadRequest, models.ErrorResponse{
				Error:   "INVALID_LGD",
				Message: fmt.Sprintf("District code %d does not exist in LGD hierarchy", *req.DistrictCode),
			})
			return
		}
	}

	if req.BlockCode != nil && req.DistrictCode != nil {
		var exists bool
		h.DB.QueryRow(context.Background(),
			`SELECT EXISTS(SELECT 1 FROM lgd_hierarchy WHERE subdistrict_code = $1 AND district_code = $2)`,
			*req.BlockCode, *req.DistrictCode).Scan(&exists)
		if !exists {
			c.JSON(http.StatusBadRequest, models.ErrorResponse{
				Error:   "INVALID_LGD",
				Message: fmt.Sprintf("Block code %d does not belong to district %d", *req.BlockCode, *req.DistrictCode),
			})
			return
		}
	}

	// Check phone uniqueness
	var phoneExists bool
	h.DB.QueryRow(context.Background(),
		`SELECT EXISTS(SELECT 1 FROM officers WHERE phone_no = $1)`,
		req.PhoneNo).Scan(&phoneExists)
	if phoneExists {
		c.JSON(http.StatusConflict, models.ErrorResponse{
			Error:   "PHONE_EXISTS",
			Message: "An officer with this phone number already exists",
		})
		return
	}

	// Insert
	callerID, _ := c.Get("officerID")
	var officerID string
	err := h.DB.QueryRow(context.Background(),
		`INSERT INTO officers (name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
		 VALUES ($1, $2, $3, $4, 3, $5, $6, $7, $8, $9)
		 RETURNING id`,
		req.Name, req.PhoneNo, req.Email, req.Role,
		req.DistrictCode, req.SubdistrictCode, req.BlockCode,
		req.Designation, callerID).Scan(&officerID)

	if err != nil {
		fmt.Printf("Officer insert error: %v\n", err)
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to create officer",
		})
		return
	}

	c.JSON(http.StatusCreated, models.OfficerResponse{
		Success: true,
		Officer: models.Officer{ID: officerID, Name: req.Name, Role: req.Role, PhoneNo: req.PhoneNo},
		Message: "Officer created successfully",
	})
}

// GetOfficers lists officers filtered by hierarchy.
// GET /api/v1/admin/officers?district_code=34&role=ADMIN_BLOCK
func (h *OfficerHandler) GetOfficers(c *gin.Context) {
	districtCode := c.Query("district_code")
	blockCode := c.Query("block_code")
	roleFilter := c.Query("role")

	callerRole, _ := c.Get("officerRole")
	callerRoleStr := fmt.Sprintf("%v", callerRole)

	// Build dynamic query with jurisdiction enforcement
	query := `SELECT o.id, o.name, o.phone_no, COALESCE(o.email,''), o.role, o.state_code,
	                 o.district_code, o.subdistrict_code, o.block_code,
	                 COALESCE(o.designation,''), o.is_active, o.created_at::text,
	                 COALESCE(d.district_name,''),
	                 COALESCE(s.subdistrict_name,''),
	                 COALESCE(cb.name,'')
	          FROM officers o
	          LEFT JOIN LATERAL (SELECT DISTINCT district_name FROM lgd_hierarchy WHERE district_code = o.district_code LIMIT 1) d ON true
	          LEFT JOIN LATERAL (SELECT DISTINCT subdistrict_name FROM lgd_hierarchy WHERE subdistrict_code = o.subdistrict_code LIMIT 1) s ON true
	          LEFT JOIN officers cb ON cb.id = o.created_by
	          WHERE 1=1`

	args := []interface{}{}
	argIdx := 1

	// Jurisdiction enforcement
	if callerRoleStr == models.RoleAdminDistrict {
		callerDistrict, _ := c.Get("officerDistrictCode")
		query += fmt.Sprintf(" AND o.district_code = $%d", argIdx)
		args = append(args, callerDistrict)
		argIdx++
	} else if callerRoleStr == models.RoleAdminSubdivision {
		callerDistrict, _ := c.Get("officerDistrictCode")
		query += fmt.Sprintf(" AND o.district_code = $%d", argIdx)
		args = append(args, callerDistrict)
		argIdx++
	}

	// Optional filters
	if districtCode != "" {
		query += fmt.Sprintf(" AND o.district_code = $%d", argIdx)
		args = append(args, districtCode)
		argIdx++
	}
	if blockCode != "" {
		query += fmt.Sprintf(" AND o.block_code = $%d", argIdx)
		args = append(args, blockCode)
		argIdx++
	}
	if roleFilter != "" {
		query += fmt.Sprintf(" AND o.role = $%d", argIdx)
		args = append(args, roleFilter)
		argIdx++
	}

	query += " ORDER BY o.created_at DESC LIMIT 100"

	rows, err := h.DB.Query(context.Background(), query, args...)
	if err != nil {
		fmt.Printf("Officer list query error: %v\n", err)
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to fetch officers",
		})
		return
	}
	defer rows.Close()

	var officers []models.Officer
	for rows.Next() {
		var o models.Officer
		rows.Scan(&o.ID, &o.Name, &o.PhoneNo, &o.Email, &o.Role, &o.StateCode,
			&o.DistrictCode, &o.SubdistrictCode, &o.BlockCode,
			&o.Designation, &o.IsActive, &o.CreatedAt,
			&o.DistrictName, &o.SubdistrictName, &o.CreatedByName)
		officers = append(officers, o)
	}

	if officers == nil {
		officers = []models.Officer{}
	}

	c.JSON(http.StatusOK, models.OfficerListResponse{
		Officers: officers,
		Total:    len(officers),
	})
}

// UpdateOfficer modifies an existing officer.
// PUT /api/v1/admin/officer/:id
func (h *OfficerHandler) UpdateOfficer(c *gin.Context) {
	officerID := c.Param("id")
	var req models.UpdateOfficerRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "INVALID_INPUT",
			Message: "Invalid request body",
		})
		return
	}

	// Fetch existing officer
	var existing models.Officer
	err := h.DB.QueryRow(context.Background(),
		`SELECT id, role, district_code, block_code FROM officers WHERE id = $1`,
		officerID).Scan(&existing.ID, &existing.Role, &existing.DistrictCode, &existing.BlockCode)
	if err != nil {
		c.JSON(http.StatusNotFound, models.ErrorResponse{
			Error:   "NOT_FOUND",
			Message: "Officer not found",
		})
		return
	}

	// Cannot modify officers of equal or higher rank
	callerRole, _ := c.Get("officerRole")
	callerRoleStr := fmt.Sprintf("%v", callerRole)
	if models.RoleHierarchy[callerRoleStr] <= models.RoleHierarchy[existing.Role] {
		c.JSON(http.StatusForbidden, models.ErrorResponse{
			Error:   "RBAC_VIOLATION",
			Message: "You cannot modify an officer of equal or higher rank",
		})
		return
	}

	// Jurisdiction check
	if !IsWithinJurisdiction(c, existing.DistrictCode, existing.BlockCode) {
		c.JSON(http.StatusForbidden, models.ErrorResponse{
			Error:   "JURISDICTION_VIOLATION",
			Message: "This officer is outside your jurisdiction",
		})
		return
	}

	// Build dynamic update
	_, err = h.DB.Exec(context.Background(),
		`UPDATE officers SET
			name = COALESCE(NULLIF($1, ''), name),
			email = COALESCE(NULLIF($2, ''), email),
			designation = COALESCE(NULLIF($3, ''), designation),
			district_code = COALESCE($4, district_code),
			subdistrict_code = COALESCE($5, subdistrict_code),
			block_code = COALESCE($6, block_code),
			is_active = COALESCE($7, is_active),
			updated_at = NOW()
		 WHERE id = $8`,
		req.Name, req.Email, req.Designation,
		req.DistrictCode, req.SubdistrictCode, req.BlockCode,
		req.IsActive, officerID)

	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to update officer",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{"success": true, "message": "Officer updated successfully"})
}

// DeactivateOfficer soft-deletes an officer.
// PATCH /api/v1/admin/officer/:id/deactivate
func (h *OfficerHandler) DeactivateOfficer(c *gin.Context) {
	officerID := c.Param("id")

	// Fetch existing
	var existing models.Officer
	err := h.DB.QueryRow(context.Background(),
		`SELECT id, role, district_code, block_code FROM officers WHERE id = $1`,
		officerID).Scan(&existing.ID, &existing.Role, &existing.DistrictCode, &existing.BlockCode)
	if err != nil {
		c.JSON(http.StatusNotFound, models.ErrorResponse{
			Error:   "NOT_FOUND",
			Message: "Officer not found",
		})
		return
	}

	callerRole, _ := c.Get("officerRole")
	callerRoleStr := fmt.Sprintf("%v", callerRole)
	if models.RoleHierarchy[callerRoleStr] <= models.RoleHierarchy[existing.Role] {
		c.JSON(http.StatusForbidden, models.ErrorResponse{
			Error:   "RBAC_VIOLATION",
			Message: "You cannot deactivate an officer of equal or higher rank",
		})
		return
	}

	if !IsWithinJurisdiction(c, existing.DistrictCode, existing.BlockCode) {
		c.JSON(http.StatusForbidden, models.ErrorResponse{
			Error:   "JURISDICTION_VIOLATION",
			Message: "This officer is outside your jurisdiction",
		})
		return
	}

	_, err = h.DB.Exec(context.Background(),
		`UPDATE officers SET is_active = FALSE, updated_at = NOW() WHERE id = $1`,
		officerID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to deactivate officer",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{"success": true, "message": "Officer deactivated"})
}

// GetBlocks returns all blocks/subdistricts (for dropdown).
// GET /api/v1/admin/blocks?district_code=34
func (h *OfficerHandler) GetBlocks(c *gin.Context) {
	districtCode := c.Query("district_code")
	if districtCode == "" {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "MISSING_PARAM",
			Message: "district_code query parameter is required",
		})
		return
	}

	rows, err := h.DB.Query(context.Background(),
		`SELECT block_code, block_name, district_code, district_name
		 FROM lgd_blocks WHERE district_code = $1 ORDER BY block_name`,
		districtCode)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to fetch blocks",
		})
		return
	}
	defer rows.Close()

	var blocks []models.LgdBlock
	for rows.Next() {
		var b models.LgdBlock
		rows.Scan(&b.BlockCode, &b.BlockName, &b.DistrictCode, &b.DistrictName)
		blocks = append(blocks, b)
	}

	if blocks == nil {
		blocks = []models.LgdBlock{}
	}

	c.JSON(http.StatusOK, gin.H{"blocks": blocks})
}
