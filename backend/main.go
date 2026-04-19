package main

import (
	"context"
	"fmt"
	"log"
	"os" // Last manual sync: 2026-04-19 (Pooler fix applied)

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/joho/godotenv"
	"pds-backend/handlers"
	"pds-backend/models"
)

func main() {
	// ─── Environment config ───────────────────────────────
	// Ignore error if .env doesn't exist (when running in container)
	_ = godotenv.Load()

	// ─── Database Connection ──────────────────────────────
	dbURL := os.Getenv("DATABASE_URL")
	if dbURL == "" {
		dbURL = "host=postgres port=5432 user=pds_admin dbname=pds_fairprice sslmode=disable"
	}

	config, err := pgxpool.ParseConfig(dbURL)
	if err != nil {
		log.Fatalf("Unable to parse config: %v\n", err)
	}

	pool, err := pgxpool.NewWithConfig(context.Background(), config)
	if err != nil {
		log.Fatalf("Unable to create pool: %v\n", err)
	}
	defer pool.Close()

	// Verify connection
	if err := pool.Ping(context.Background()); err != nil {
		log.Fatalf("Database ping failed: %v\n", err)
	}
	log.Println("✅ Connected to PostgreSQL (pds_fairprice)")

	// ─── Gin Router ───────────────────────────────────────
	router := gin.Default()

	// CORS — allow Android app
	router.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"*"},
		AllowMethods:     []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Origin", "Content-Type", "Authorization"},
		AllowCredentials: true,
	}))

	// ─── Supabase Config ──────────────────────────────────
	supabaseURL := os.Getenv("SUPABASE_URL")
	supabaseKey := os.Getenv("SUPABASE_KEY")
	jwtSecret := os.Getenv("SUPABASE_JWT_SECRET")
	if jwtSecret == "" {
		fmt.Printf("[CRITICAL] SUPABASE_JWT_SECRET is not set\n")
	} else {
		fmt.Printf("[INFO] JWT configuration loaded (length: %d)\n", len(jwtSecret))
	}

	// ─── Handlers ─────────────────────────────────────────
	authHandler := handlers.NewAuthHandler(pool, supabaseURL, supabaseKey, jwtSecret)
	lgdHandler := handlers.NewLgdHandler(pool)
	pollHandler := handlers.NewPollHandler(pool)
	officerHandler := handlers.NewOfficerHandler(pool)
	analyticsHandler := handlers.NewAnalyticsHandler(pool)

	// ─── Routes ───────────────────────────────────────────
	api := router.Group("/api/v1")
	{
		// Auth endpoints
		auth := api.Group("/auth")
		{
			auth.POST("/login", authHandler.Login)
			auth.POST("/verify-otp", authHandler.VerifyOtp)
			auth.POST("/officer-login", authHandler.OfficerLogin)
			auth.POST("/register-profile", handlers.SupabaseAuthMiddleware(jwtSecret), authHandler.RegisterProfile)
		}

		// LGD hierarchy endpoints (Public for registration/setup)
		lgd := api.Group("/lgd")
		{
			lgd.GET("/districts", lgdHandler.GetDistricts)
			lgd.GET("/subdistricts", lgdHandler.GetSubdistricts)
			lgd.GET("/villages", lgdHandler.GetVillages)
		}

		// Poll endpoints (all protected by JWT)
		polls := api.Group("/polls")
		polls.Use(handlers.SupabaseAuthMiddleware(jwtSecret))
		{
			polls.GET("", pollHandler.GetActivePolls)
			polls.GET("/my-responses", pollHandler.GetMyResponses)
			polls.POST("/submit", handlers.PollRateLimitMiddleware(), pollHandler.SubmitPoll)
		}

		// Admin / Officer Management (JWT + RBAC)
		admin := api.Group("/admin")
		admin.Use(handlers.SupabaseAuthMiddleware(jwtSecret))
		admin.Use(handlers.RBACMiddleware(pool, models.RoleAdminBlock))
		{
			// Officer CRUD
			admin.POST("/create-officer", officerHandler.CreateOfficer)
			admin.GET("/officers", officerHandler.GetOfficers)
			admin.PUT("/officer/:id", officerHandler.UpdateOfficer)
			admin.PATCH("/officer/:id/deactivate", officerHandler.DeactivateOfficer)

			// LGD blocks for admin dropdowns
			admin.GET("/blocks", officerHandler.GetBlocks)

			// Poll management (hierarchy-scoped)
			admin.POST("/create-poll", pollHandler.CreatePoll)
			admin.GET("/polls", pollHandler.GetPollAnalytics)
			admin.PATCH("/poll/:id/close", pollHandler.ClosePoll)

			// Analytics engine (Phases 5-8)
			admin.GET("/analytics/summary", analyticsHandler.GetAnalyticsSummary)
			admin.GET("/analytics/poll/:id", analyticsHandler.GetPollAnalyticsDetail)
			admin.GET("/analytics/zones", analyticsHandler.GetZoneClassification)
		}
	}

	// Health check
	router.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{"status": "ok", "service": "pds-fairprice-backend"})
	})

	// ─── Start Server ─────────────────────────────────────
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}
	log.Printf("🚀 PDS Fair Price Backend starting on :%s\n", port)
	if err := router.Run(fmt.Sprintf(":%s", port)); err != nil {
		log.Fatalf("Failed to start server: %v\n", err)
	}
}
