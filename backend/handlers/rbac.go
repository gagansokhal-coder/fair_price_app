package handlers

import (
	"context"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
	"pds-backend/models"
)

// RBACMiddleware enforces role-based access control.
// It reads the userID from the JWT context, queries the officer table,
// and injects the officer's role + jurisdiction into the Gin context.
// minRole specifies the minimum required role level.
func RBACMiddleware(db *pgxpool.Pool, minRole string) gin.HandlerFunc {
	return func(c *gin.Context) {
		userID, exists := c.Get("userID")
		if !exists {
			c.JSON(http.StatusUnauthorized, models.ErrorResponse{
				Error:   "AUTH_MISSING",
				Message: "User identity not found in token",
			})
			c.Abort()
			return
		}

		userIDStr := fmt.Sprintf("%v", userID)

		// Query officer record
		var officer models.Officer
		var districtCode, subdistrictCode, blockCode *int
		err := db.QueryRow(context.Background(),
			`SELECT id, name, role, state_code, district_code, subdistrict_code, block_code, is_active
			 FROM officers WHERE id = $1 OR phone_no = $1`,
			userIDStr).Scan(
			&officer.ID, &officer.Name, &officer.Role,
			&officer.StateCode, &districtCode, &subdistrictCode, &blockCode,
			&officer.IsActive,
		)

		if err != nil {
			c.JSON(http.StatusForbidden, models.ErrorResponse{
				Error:   "NOT_OFFICER",
				Message: "You are not registered as an officer in the system",
			})
			c.Abort()
			return
		}

		if !officer.IsActive {
			c.JSON(http.StatusForbidden, models.ErrorResponse{
				Error:   "OFFICER_DEACTIVATED",
				Message: "Your officer account has been deactivated",
			})
			c.Abort()
			return
		}

		officer.DistrictCode = districtCode
		officer.SubdistrictCode = subdistrictCode
		officer.BlockCode = blockCode

		// Check role hierarchy
		callerLevel := models.RoleHierarchy[officer.Role]
		requiredLevel := models.RoleHierarchy[minRole]

		if callerLevel < requiredLevel {
			c.JSON(http.StatusForbidden, models.ErrorResponse{
				Error:   "INSUFFICIENT_ROLE",
				Message: fmt.Sprintf("Your role (%s) cannot perform this action. Minimum required: %s", officer.Role, minRole),
			})
			c.Abort()
			return
		}

		// Inject officer context for downstream handlers
		c.Set("officerID", officer.ID)
		c.Set("officerRole", officer.Role)
		c.Set("officerStateCode", officer.StateCode)
		if districtCode != nil {
			c.Set("officerDistrictCode", *districtCode)
		}
		if subdistrictCode != nil {
			c.Set("officerSubdistrictCode", *subdistrictCode)
		}
		if blockCode != nil {
			c.Set("officerBlockCode", *blockCode)
		}

		c.Next()
	}
}

// CanCreateRole checks if callerRole can create targetRole.
// Rule: You can only create roles STRICTLY below your own level.
func CanCreateRole(callerRole, targetRole string) bool {
	callerLevel := models.RoleHierarchy[callerRole]
	targetLevel := models.RoleHierarchy[targetRole]
	return callerLevel > targetLevel
}

// IsWithinJurisdiction checks if the caller officer has authority over the target LGD codes.
func IsWithinJurisdiction(c *gin.Context, targetDistrictCode, targetBlockCode *int) bool {
	callerRole, _ := c.Get("officerRole")
	role := fmt.Sprintf("%v", callerRole)

	// State admin has full jurisdiction
	if role == models.RoleAdminState {
		return true
	}

	// District admin — target must be within their district
	if role == models.RoleAdminDistrict {
		callerDistrict, exists := c.Get("officerDistrictCode")
		if !exists || targetDistrictCode == nil {
			return false
		}
		return callerDistrict.(int) == *targetDistrictCode
	}

	// Subdivision admin — target district must match AND block must be in their subdivision
	if role == models.RoleAdminSubdivision {
		callerDistrict, d := c.Get("officerDistrictCode")
		callerSubdistrict, s := c.Get("officerSubdistrictCode")
		if !d || !s || targetDistrictCode == nil {
			return false
		}
		if callerDistrict.(int) != *targetDistrictCode {
			return false
		}
		// If targeting a specific block, it must be within their subdistrict
		if targetBlockCode != nil && callerSubdistrict.(int) != *targetBlockCode {
			return false
		}
		return true
	}

	// Block admin — cannot create officers
	return false
}
