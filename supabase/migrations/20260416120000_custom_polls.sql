-- ============================================================
-- PDS Fair Price App — Custom Dynamic Polling System
-- Migration 003: Replaces hardcoded commodity polls with
-- fully dynamic, hierarchy-targeted custom polls.
-- ============================================================

-- ─── Drop old poll tables if they exist ─────────────────────
DROP TABLE IF EXISTS poll_responses CASCADE;
DROP TABLE IF EXISTS active_polls CASCADE;

-- ─── Custom Polls ───────────────────────────────────────────
-- Fully dynamic polls with arbitrary options (2-5 choices).
-- Officers create polls scoped to their LGD jurisdiction.
CREATE TABLE custom_polls (
    poll_id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title           VARCHAR(500) NOT NULL,
    description     TEXT,
    created_by      UUID NOT NULL REFERENCES officers(id),

    -- Targeting: which LGD level + code this poll covers
    -- DISTRICT, SUBDIVISION, BLOCK, VILLAGE
    target_level    VARCHAR(20) NOT NULL CHECK (target_level IN ('DISTRICT', 'SUBDIVISION', 'BLOCK', 'VILLAGE')),
    target_code     INTEGER NOT NULL,

    -- Dynamic options stored as JSONB array of strings
    -- e.g. ["Yes", "No"] or ["Dose 1", "Dose 2", "Booster", "Not received"]
    options         JSONB NOT NULL DEFAULT '[]'::jsonb,

    -- Poll lifecycle
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Constraints
    CONSTRAINT chk_options_length CHECK (
        jsonb_array_length(options) >= 2 AND jsonb_array_length(options) <= 5
    )
);

-- Indexes for poll lookup
CREATE INDEX idx_polls_target ON custom_polls (target_level, target_code);
CREATE INDEX idx_polls_active ON custom_polls (is_active) WHERE is_active = TRUE;
CREATE INDEX idx_polls_created_by ON custom_polls (created_by);

-- ─── Poll Responses ─────────────────────────────────────────
-- Citizens submit their selected option (index into the options array).
CREATE TABLE custom_poll_responses (
    response_id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    poll_id                 UUID NOT NULL REFERENCES custom_polls(poll_id) ON DELETE CASCADE,
    user_id                 UUID NOT NULL REFERENCES users(id),

    -- The option the citizen selected (0-indexed into options array)
    selected_option_index   INTEGER NOT NULL,
    -- Denormalized text for query convenience
    selected_option_text    VARCHAR(200) NOT NULL,

    -- GPS verification (PostGIS geofence)
    gps_lat                 DOUBLE PRECISION,
    gps_lng                 DOUBLE PRECISION,
    distance_from_shop_meters DOUBLE PRECISION,

    submitted_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Each citizen can only respond once per poll
    CONSTRAINT uq_poll_user UNIQUE (poll_id, user_id),
    -- Option index must be valid (0-4)
    CONSTRAINT chk_option_index CHECK (selected_option_index >= 0 AND selected_option_index <= 4)
);

CREATE INDEX idx_responses_poll ON custom_poll_responses (poll_id);
CREATE INDEX idx_responses_user ON custom_poll_responses (user_id);

-- ─── Analytics View: Poll Results Summary ───────────────────
-- Aggregates vote counts per option for each poll.
CREATE OR REPLACE VIEW poll_results_summary AS
SELECT
    cp.poll_id,
    cp.title,
    cp.target_level,
    cp.target_code,
    cp.options,
    cp.is_active,
    cp.created_at,
    COUNT(cpr.response_id) AS total_responses,
    jsonb_object_agg(
        COALESCE(cpr.selected_option_text, 'none'),
        COALESCE(option_counts.cnt, 0)
    ) FILTER (WHERE cpr.response_id IS NOT NULL) AS option_breakdown
FROM custom_polls cp
LEFT JOIN custom_poll_responses cpr ON cp.poll_id = cpr.poll_id
LEFT JOIN (
    SELECT poll_id, selected_option_text, COUNT(*) AS cnt
    FROM custom_poll_responses
    GROUP BY poll_id, selected_option_text
) option_counts ON option_counts.poll_id = cp.poll_id
    AND option_counts.selected_option_text = cpr.selected_option_text
GROUP BY cp.poll_id, cp.title, cp.target_level, cp.target_code,
         cp.options, cp.is_active, cp.created_at;

-- ─── LGD Blocks view (convenience for admin dropdowns) ──────
CREATE OR REPLACE VIEW lgd_blocks AS
SELECT DISTINCT
    subdistrict_code AS block_code,
    subdistrict_name AS block_name,
    district_code,
    district_name
FROM lgd_hierarchy
ORDER BY district_name, subdistrict_name;

-- ─── LGD Villages view ─────────────────────────────────────
CREATE OR REPLACE VIEW lgd_villages_summary AS
SELECT DISTINCT
    village_code,
    village_name,
    subdistrict_code,
    subdistrict_name,
    district_code,
    district_name
FROM lgd_hierarchy
ORDER BY district_name, subdistrict_name, village_name;
