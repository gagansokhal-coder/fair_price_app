package main

import (
	"context"
	"fmt"
	"log"
	"os"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
	"pds-backend/handlers"
)

func main() {
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

	// ─── Handlers ─────────────────────────────────────────
	authHandler := handlers.NewAuthHandler(pool, supabaseURL, supabaseKey)
	lgdHandler := handlers.NewLgdHandler(pool)

	// ─── Routes ───────────────────────────────────────────
	api := router.Group("/api/v1")
	{
		// Auth endpoints
		auth := api.Group("/auth")
		{
			auth.POST("/login", authHandler.Login)
			auth.POST("/verify-otp", authHandler.VerifyOtp)
			auth.POST("/register-profile", handlers.SupabaseAuthMiddleware(jwtSecret), authHandler.RegisterProfile)
		}

		// LGD hierarchy endpoints
		lgd := api.Group("/lgd")
		lgd.Use(handlers.SupabaseAuthMiddleware(jwtSecret))
		{
			lgd.GET("/districts", lgdHandler.GetDistricts)
			lgd.GET("/subdistricts", lgdHandler.GetSubdistricts)
			lgd.GET("/villages", lgdHandler.GetVillages)
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
