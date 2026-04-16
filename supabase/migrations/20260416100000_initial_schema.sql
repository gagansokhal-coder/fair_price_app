-- ============================================================
-- PDS Fair Price App — Initial Schema
-- PostgreSQL 15 + PostGIS
-- ============================================================

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- ─── LGD Hierarchy (Punjab State) ────────────────────────────
-- Denormalized for mobile read-efficiency.
-- Each row = one village with its full hierarchy path.
CREATE TABLE lgd_hierarchy (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    state_code      INTEGER NOT NULL,
    state_name      VARCHAR(100) NOT NULL,
    district_code   INTEGER NOT NULL,
    district_name   VARCHAR(100) NOT NULL,
    subdistrict_code INTEGER NOT NULL,
    subdistrict_name VARCHAR(100) NOT NULL,
    village_code    INTEGER NOT NULL,
    village_name    VARCHAR(200) NOT NULL,
    pincode         VARCHAR(10),
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for cascading dropdown queries
CREATE INDEX idx_lgd_district ON lgd_hierarchy (state_code, district_code);
CREATE INDEX idx_lgd_subdistrict ON lgd_hierarchy (district_code, subdistrict_code);
CREATE INDEX idx_lgd_village ON lgd_hierarchy (subdistrict_code, village_code);

-- ─── Users ───────────────────────────────────────────────────
-- Citizen users registered via Ration Card + Phone.
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ration_card_no  VARCHAR(12) NOT NULL UNIQUE,
    phone_no        VARCHAR(10) NOT NULL,
    full_name       VARCHAR(200),
    address         TEXT,
    role            VARCHAR(20) NOT NULL DEFAULT 'CITIZEN',
    -- LGD hierarchy reference
    state_code      INTEGER,
    district_code   INTEGER,
    subdistrict_code INTEGER,
    village_code    INTEGER,
    -- Device binding
    hardware_uuid   VARCHAR(100) UNIQUE,
    -- GPS location at registration
    gps_lat         DOUBLE PRECISION,
    gps_lng         DOUBLE PRECISION,
    -- Profile completion flag
    profile_complete BOOLEAN NOT NULL DEFAULT FALSE,
    -- Timestamps
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

-- Index for login lookups
CREATE INDEX idx_users_ration_card ON users (ration_card_no);
CREATE INDEX idx_users_phone ON users (phone_no);
CREATE INDEX idx_users_hardware ON users (hardware_uuid);

-- ─── OTP Sessions (simulated) ────────────────────────────────
CREATE TABLE otp_sessions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    phone_no        VARCHAR(10) NOT NULL,
    otp_code        VARCHAR(6) NOT NULL DEFAULT '123456',
    is_verified     BOOLEAN DEFAULT FALSE,
    expires_at      TIMESTAMPTZ DEFAULT (NOW() + INTERVAL '5 minutes'),
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_otp_phone ON otp_sessions (phone_no, is_verified);
