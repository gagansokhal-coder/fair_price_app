package handlers

import (
	"testing"
)

// ═══════════════════════════════════════════════════════════════
// UNIT TESTS: Analytics Engine — Zone Classification
// ═══════════════════════════════════════════════════════════════

func TestClassifyZone_Green(t *testing.T) {
	tests := []struct {
		pct  float64
		want string
	}{
		{100.0, "GREEN"},
		{70.0, "GREEN"},
		{85.5, "GREEN"},
	}
	for _, tt := range tests {
		got := classifyZone(tt.pct)
		if got != tt.want {
			t.Errorf("classifyZone(%.1f) = %s, want %s", tt.pct, got, tt.want)
		}
	}
}

func TestClassifyZone_Yellow(t *testing.T) {
	tests := []struct {
		pct  float64
		want string
	}{
		{69.9, "YELLOW"},
		{40.0, "YELLOW"},
		{55.0, "YELLOW"},
	}
	for _, tt := range tests {
		got := classifyZone(tt.pct)
		if got != tt.want {
			t.Errorf("classifyZone(%.1f) = %s, want %s", tt.pct, got, tt.want)
		}
	}
}

func TestClassifyZone_Red(t *testing.T) {
	tests := []struct {
		pct  float64
		want string
	}{
		{39.9, "RED"},
		{0.0, "RED"},
		{10.0, "RED"},
	}
	for _, tt := range tests {
		got := classifyZone(tt.pct)
		if got != tt.want {
			t.Errorf("classifyZone(%.1f) = %s, want %s", tt.pct, got, tt.want)
		}
	}
}

func TestClassifyZone_BoundaryValues(t *testing.T) {
	// Exact boundaries
	if classifyZone(70.0) != "GREEN" {
		t.Error("70.0 should be GREEN (>= 70)")
	}
	if classifyZone(40.0) != "YELLOW" {
		t.Error("40.0 should be YELLOW (>= 40)")
	}
	if classifyZone(39.999) != "RED" {
		t.Error("39.999 should be RED (< 40)")
	}
}
