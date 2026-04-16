package handlers

import (
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
)

// ═══════════════════════════════════════════════════════════════
// UNIT TESTS: Response Collection — Phase 4
//
// Tests cover:
//  1. Rate limiter logic (per-user 60s window)
//  2. isPgUniqueViolation helper
//  3. SubmitPoll handler input validation
// ═══════════════════════════════════════════════════════════════

func TestRateLimiter_AllowsFirstRequest(t *testing.T) {
	rl := &rateLimiter{lastVote: make(map[string]time.Time)}

	if !rl.CheckRateLimit("user-1") {
		t.Error("Expected first request to be allowed, but it was rate-limited")
	}
}

func TestRateLimiter_BlocksSecondRequestWithin60s(t *testing.T) {
	rl := &rateLimiter{lastVote: make(map[string]time.Time)}

	// First request: should pass
	if !rl.CheckRateLimit("user-1") {
		t.Fatal("First request should pass")
	}

	// Second request immediately: should be blocked
	if rl.CheckRateLimit("user-1") {
		t.Error("Expected second request within 60s to be blocked, but it was allowed")
	}
}

func TestRateLimiter_AllowsDifferentUsers(t *testing.T) {
	rl := &rateLimiter{lastVote: make(map[string]time.Time)}

	if !rl.CheckRateLimit("user-1") {
		t.Error("user-1 first request should be allowed")
	}

	if !rl.CheckRateLimit("user-2") {
		t.Error("user-2 first request should be allowed (different user)")
	}
}

func TestRateLimiter_AllowsAfterCooldown(t *testing.T) {
	rl := &rateLimiter{lastVote: make(map[string]time.Time)}

	// Set last vote to 61 seconds ago
	rl.lastVote["user-1"] = time.Now().Add(-61 * time.Second)

	if !rl.CheckRateLimit("user-1") {
		t.Error("Expected request after 61s cooldown to be allowed, but it was blocked")
	}
}

func TestIsPgUniqueViolation_WithNilError(t *testing.T) {
	if isPgUniqueViolation(nil) {
		t.Error("nil error should not be a unique violation")
	}
}

func TestIsPgUniqueViolation_WithGenericError(t *testing.T) {
	err := fmt.Errorf("some generic error")
	if isPgUniqueViolation(err) {
		t.Error("generic error should not be a unique violation")
	}
}

func TestPollRateLimitMiddleware_Returns429WhenLimited(t *testing.T) {
	gin.SetMode(gin.TestMode)

	router := gin.New()
	router.Use(func(c *gin.Context) {
		c.Set("userID", "test-user-rate-limited")
		c.Next()
	})

	// Create a separate rate limiter instance and pre-fill it
	testRL := &rateLimiter{lastVote: make(map[string]time.Time)}
	testRL.lastVote["test-user-rate-limited"] = time.Now()

	// Swap global rate limiter for test
	origRL := pollRateLimiter
	pollRateLimiter = testRL
	defer func() { pollRateLimiter = origRL }()

	router.POST("/submit", PollRateLimitMiddleware(), func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"success": true})
	})

	req := httptest.NewRequest("POST", "/submit", strings.NewReader(`{}`))
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusTooManyRequests {
		t.Errorf("Expected 429 Too Many Requests, got %d", w.Code)
	}

	var body map[string]string
	json.Unmarshal(w.Body.Bytes(), &body)
	if body["error"] != "RATE_LIMITED" {
		t.Errorf("Expected error code RATE_LIMITED, got %s", body["error"])
	}
}

func TestPollRateLimitMiddleware_AllowsFirstRequest(t *testing.T) {
	gin.SetMode(gin.TestMode)

	router := gin.New()
	router.Use(func(c *gin.Context) {
		c.Set("userID", "test-user-fresh")
		c.Next()
	})

	// Fresh rate limiter
	testRL := &rateLimiter{lastVote: make(map[string]time.Time)}
	origRL := pollRateLimiter
	pollRateLimiter = testRL
	defer func() { pollRateLimiter = origRL }()

	handlerCalled := false
	router.POST("/submit", PollRateLimitMiddleware(), func(c *gin.Context) {
		handlerCalled = true
		c.JSON(http.StatusOK, gin.H{"success": true})
	})

	req := httptest.NewRequest("POST", "/submit", strings.NewReader(`{}`))
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Errorf("Expected 200 OK, got %d", w.Code)
	}
	if !handlerCalled {
		t.Error("Handler should have been called for first request")
	}
}

func TestPollRateLimitMiddleware_Returns401WhenNoUser(t *testing.T) {
	gin.SetMode(gin.TestMode)

	router := gin.New()
	// No userID set in context
	router.POST("/submit", PollRateLimitMiddleware(), func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"success": true})
	})

	req := httptest.NewRequest("POST", "/submit", strings.NewReader(`{}`))
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusUnauthorized {
		t.Errorf("Expected 401 Unauthorized, got %d", w.Code)
	}
}
