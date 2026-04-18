package handlers

import (
	"context"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
	"pds-backend/models"
)

// LgdHandler handles LGD hierarchy API endpoints.
type LgdHandler struct {
	DB *pgxpool.Pool
}

// NewLgdHandler creates a new LGD handler.
func NewLgdHandler(db *pgxpool.Pool) *LgdHandler {
	return &LgdHandler{DB: db}
}

// GetDistricts returns all distinct districts for Punjab (state_code=3).
// GET /api/v1/lgd/districts
func (h *LgdHandler) GetDistricts(c *gin.Context) {
	rows, err := h.DB.Query(context.Background(),
		`SELECT DISTINCT district_code, district_name 
		 FROM lgd_hierarchy 
		 WHERE state_code = 3 
		 ORDER BY district_name`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to fetch districts",
		})
		return
	}
	defer rows.Close()

	items := []models.LgdItem{}
	for rows.Next() {
		var item models.LgdItem
		if err := rows.Scan(&item.Code, &item.Name); err != nil {
			continue
		}
		items = append(items, item)
	}

	c.JSON(http.StatusOK, gin.H{"districts": items})
}

// GetSubdistricts returns sub-districts for a given district code.
// GET /api/v1/lgd/subdistricts?district_code=34
func (h *LgdHandler) GetSubdistricts(c *gin.Context) {
	districtCode := c.Query("district_code")
	if districtCode == "" {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "MISSING_PARAM",
			Message: "district_code query parameter is required",
		})
		return
	}

	rows, err := h.DB.Query(context.Background(),
		`SELECT DISTINCT subdistrict_code, subdistrict_name 
		 FROM lgd_hierarchy 
		 WHERE district_code = $1 
		 ORDER BY subdistrict_name`, districtCode)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to fetch sub-districts",
		})
		return
	}
	defer rows.Close()

	items := []models.LgdItem{}
	for rows.Next() {
		var item models.LgdItem
		if err := rows.Scan(&item.Code, &item.Name); err != nil {
			continue
		}
		items = append(items, item)
	}

	c.JSON(http.StatusOK, gin.H{"subdistricts": items})
}

// GetVillages returns villages for a given sub-district code.
// GET /api/v1/lgd/villages?subdistrict_code=210
func (h *LgdHandler) GetVillages(c *gin.Context) {
	subdistrictCode := c.Query("subdistrict_code")
	if subdistrictCode == "" {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "MISSING_PARAM",
			Message: "subdistrict_code query parameter is required",
		})
		return
	}

	rows, err := h.DB.Query(context.Background(),
		`SELECT DISTINCT village_code, village_name 
		 FROM lgd_hierarchy 
		 WHERE subdistrict_code = $1 
		 ORDER BY village_name`, subdistrictCode)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "DB_ERROR",
			Message: "Failed to fetch villages",
		})
		return
	}
	defer rows.Close()

	items := []models.LgdItem{}
	for rows.Next() {
		var item models.LgdItem
		if err := rows.Scan(&item.Code, &item.Name); err != nil {
			continue
		}
		items = append(items, item)
	}

	c.JSON(http.StatusOK, gin.H{"villages": items})
}
