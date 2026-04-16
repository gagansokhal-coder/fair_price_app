-- ============================================================
-- PDS Fair Price App — Officers Table + Seed Data
-- Migration 004: Creates officers table and seeds real LGD-mapped
-- officer data for 4 Punjab districts.
-- ============================================================

-- ─── Officers Table ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS officers (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name              VARCHAR(200) NOT NULL,
    phone_no          VARCHAR(10) NOT NULL UNIQUE,
    email             VARCHAR(200),
    role              VARCHAR(30) NOT NULL CHECK (role IN (
        'ADMIN_STATE', 'ADMIN_DISTRICT', 'ADMIN_SUBDIVISION', 'ADMIN_BLOCK'
    )),
    state_code        INTEGER NOT NULL DEFAULT 3,  -- Punjab
    district_code     INTEGER,
    subdistrict_code  INTEGER,
    block_code        INTEGER,
    designation       VARCHAR(100),
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_by        UUID,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_officers_phone ON officers (phone_no);
CREATE INDEX IF NOT EXISTS idx_officers_role ON officers (role);
CREATE INDEX IF NOT EXISTS idx_officers_district ON officers (district_code);

-- ─── State-Level Officer ─────────────────────────────────────
-- Punjab Commissioner (full state access)
INSERT INTO officers (id, name, phone_no, email, role, state_code, designation)
VALUES (
    'a0000001-0000-0000-0000-000000000001',
    'Sh. Harpreet Singh Sidhu',
    '9800000001',
    'commissioner.pds@punjab.gov.in',
    'ADMIN_STATE', 3,
    'Commissioner Food & Civil Supplies, Punjab'
);

-- ═══════════════════════════════════════════════════════════════
-- JALANDHAR (District Code: 34)
-- Subdistricts: Nakodar(210), Phillaur(211), Shahkot(209),
--               Adampur(7192), Jalandhar-I(212), Jalandhar-II(213)
-- ═══════════════════════════════════════════════════════════════

-- DM Jalandhar
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, designation, created_by)
VALUES (
    'b0000001-0001-0000-0000-000000000034',
    'Sh. Rajat Aggarwal IAS',
    '9800100001',
    'dm.jalandhar@punjab.gov.in',
    'ADMIN_DISTRICT', 3, 34,
    'Deputy Commissioner, Jalandhar',
    'a0000001-0000-0000-0000-000000000001'
);

-- SDO Nakodar (210)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0210-0000-000000000034',
    'Sh. Gurpreet Kaur PCS',
    '9800100010',
    'sdo.nakodar@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 34, 210,
    'Sub-Divisional Officer, Nakodar',
    'b0000001-0001-0000-0000-000000000034'
);

-- BDO Nakodar (210)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0210-0001-000000000034',
    'Sh. Amanjot Singh',
    '9800100011',
    'bdo.nakodar@punjab.gov.in',
    'ADMIN_BLOCK', 3, 34, 210, 210,
    'Block Development Officer, Nakodar',
    'c0000001-0001-0210-0000-000000000034'
);

-- SDO Phillaur (211)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0211-0000-000000000034',
    'Sh. Mandeep Kaur PCS',
    '9800100020',
    'sdo.phillaur@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 34, 211,
    'Sub-Divisional Officer, Phillaur',
    'b0000001-0001-0000-0000-000000000034'
);

-- BDO Phillaur (211)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0211-0001-000000000034',
    'Sh. Harjinder Pal',
    '9800100021',
    'bdo.phillaur@punjab.gov.in',
    'ADMIN_BLOCK', 3, 34, 211, 211,
    'Block Development Officer, Phillaur',
    'c0000001-0001-0211-0000-000000000034'
);

-- SDO Shahkot (209)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0209-0000-000000000034',
    'Sh. Parminder Kaur PCS',
    '9800100030',
    'sdo.shahkot@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 34, 209,
    'Sub-Divisional Officer, Shahkot',
    'b0000001-0001-0000-0000-000000000034'
);

-- BDO Shahkot (209)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0209-0001-000000000034',
    'Sh. Sukhwinder Kaur',
    '9800100031',
    'bdo.shahkot@punjab.gov.in',
    'ADMIN_BLOCK', 3, 34, 209, 209,
    'Block Development Officer, Shahkot',
    'c0000001-0001-0209-0000-000000000034'
);

-- SDO Adampur (7192)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-7192-0000-000000000034',
    'Sh. Navjot Singh PCS',
    '9800100040',
    'sdo.adampur@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 34, 7192,
    'Sub-Divisional Officer, Adampur',
    'b0000001-0001-0000-0000-000000000034'
);

-- BDO Adampur (7192)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-7192-0001-000000000034',
    'Sh. Balwinder Kumar',
    '9800100041',
    'bdo.adampur@punjab.gov.in',
    'ADMIN_BLOCK', 3, 34, 7192, 7192,
    'Block Development Officer, Adampur',
    'c0000001-0001-7192-0000-000000000034'
);

-- SDO Jalandhar-I (212)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0212-0000-000000000034',
    'Sh. Ravinder Pal Singh PCS',
    '9800100050',
    'sdo.jalandhar1@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 34, 212,
    'Sub-Divisional Officer, Jalandhar-I',
    'b0000001-0001-0000-0000-000000000034'
);

-- BDO Jalandhar-I (212)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0212-0001-000000000034',
    'Sh. Kuldeep Rani',
    '9800100051',
    'bdo.jalandhar1@punjab.gov.in',
    'ADMIN_BLOCK', 3, 34, 212, 212,
    'Block Development Officer, Jalandhar-I',
    'c0000001-0001-0212-0000-000000000034'
);

-- SDO Jalandhar-II (213)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0213-0000-000000000034',
    'Sh. Jaswant Singh PCS',
    '9800100060',
    'sdo.jalandhar2@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 34, 213,
    'Sub-Divisional Officer, Jalandhar-II',
    'b0000001-0001-0000-0000-000000000034'
);

-- BDO Jalandhar-II (213)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0213-0001-000000000034',
    'Sh. Rajwinder Kaur',
    '9800100061',
    'bdo.jalandhar2@punjab.gov.in',
    'ADMIN_BLOCK', 3, 34, 213, 213,
    'Block Development Officer, Jalandhar-II',
    'c0000001-0001-0213-0000-000000000034'
);

-- ═══════════════════════════════════════════════════════════════
-- FAZILKA (District Code: 651)
-- Subdistricts: Fazilka(237), Jalalabad(236), Abohar(238)
-- ═══════════════════════════════════════════════════════════════

-- DM Fazilka
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, designation, created_by)
VALUES (
    'b0000001-0001-0000-0000-000000000651',
    'Sh. Senu Duggal IAS',
    '9800200001',
    'dm.fazilka@punjab.gov.in',
    'ADMIN_DISTRICT', 3, 651,
    'Deputy Commissioner, Fazilka',
    'a0000001-0000-0000-0000-000000000001'
);

-- SDO Fazilka (237)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0237-0000-000000000651',
    'Sh. Amandeep Bansal PCS',
    '9800200010',
    'sdo.fazilka@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 651, 237,
    'Sub-Divisional Officer, Fazilka',
    'b0000001-0001-0000-0000-000000000651'
);

-- BDO Fazilka (237)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0237-0001-000000000651',
    'Sh. Mohinder Singh',
    '9800200011',
    'bdo.fazilka@punjab.gov.in',
    'ADMIN_BLOCK', 3, 651, 237, 237,
    'Block Development Officer, Fazilka',
    'c0000001-0001-0237-0000-000000000651'
);

-- SDO Jalalabad (236)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0236-0000-000000000651',
    'Sh. Paramjit Kaur PCS',
    '9800200020',
    'sdo.jalalabad@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 651, 236,
    'Sub-Divisional Officer, Jalalabad',
    'b0000001-0001-0000-0000-000000000651'
);

-- BDO Jalalabad (236)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0236-0001-000000000651',
    'Sh. Ranjit Singh',
    '9800200021',
    'bdo.jalalabad@punjab.gov.in',
    'ADMIN_BLOCK', 3, 651, 236, 236,
    'Block Development Officer, Jalalabad',
    'c0000001-0001-0236-0000-000000000651'
);

-- SDO Abohar (238)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0238-0000-000000000651',
    'Sh. Sandeep Kaur PCS',
    '9800200030',
    'sdo.abohar@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 651, 238,
    'Sub-Divisional Officer, Abohar',
    'b0000001-0001-0000-0000-000000000651'
);

-- BDO Abohar (238)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0238-0001-000000000651',
    'Sh. Jagtar Singh',
    '9800200031',
    'bdo.abohar@punjab.gov.in',
    'ADMIN_BLOCK', 3, 651, 238, 238,
    'Block Development Officer, Abohar',
    'c0000001-0001-0238-0000-000000000651'
);

-- ═══════════════════════════════════════════════════════════════
-- AMRITSAR (District Code: 27)
-- Subdistricts: Ajnala(255), Amritsar-I(256), Amritsar-II(257),
--               Baba Bakala(258), Lopoke(7341), Majitha(6854)
-- ═══════════════════════════════════════════════════════════════

-- DM Amritsar
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, designation, created_by)
VALUES (
    'b0000001-0001-0000-0000-000000000027',
    'Sh. Harpreet Singh Sudan IAS',
    '9800300001',
    'dm.amritsar@punjab.gov.in',
    'ADMIN_DISTRICT', 3, 27,
    'Deputy Commissioner, Amritsar',
    'a0000001-0000-0000-0000-000000000001'
);

-- SDO Ajnala (255)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0255-0000-000000000027',
    'Sh. Karanveer Singh PCS',
    '9800300010',
    'sdo.ajnala@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 27, 255,
    'Sub-Divisional Officer, Ajnala',
    'b0000001-0001-0000-0000-000000000027'
);

-- BDO Ajnala (255)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0255-0001-000000000027',
    'Sh. Gurdeep Kaur',
    '9800300011',
    'bdo.ajnala@punjab.gov.in',
    'ADMIN_BLOCK', 3, 27, 255, 255,
    'Block Development Officer, Ajnala',
    'c0000001-0001-0255-0000-000000000027'
);

-- SDO Amritsar-I (256)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0256-0000-000000000027',
    'Sh. Satwinder Singh PCS',
    '9800300020',
    'sdo.amritsar1@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 27, 256,
    'Sub-Divisional Officer, Amritsar-I',
    'b0000001-0001-0000-0000-000000000027'
);

-- BDO Amritsar-I (256)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0256-0001-000000000027',
    'Sh. Rajinder Kumar',
    '9800300021',
    'bdo.amritsar1@punjab.gov.in',
    'ADMIN_BLOCK', 3, 27, 256, 256,
    'Block Development Officer, Amritsar-I',
    'c0000001-0001-0256-0000-000000000027'
);

-- SDO Amritsar-II (257)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0257-0000-000000000027',
    'Sh. Navneet Kaur PCS',
    '9800300030',
    'sdo.amritsar2@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 27, 257,
    'Sub-Divisional Officer, Amritsar-II',
    'b0000001-0001-0000-0000-000000000027'
);

-- BDO Amritsar-II (257)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0257-0001-000000000027',
    'Sh. Parkash Chand',
    '9800300031',
    'bdo.amritsar2@punjab.gov.in',
    'ADMIN_BLOCK', 3, 27, 257, 257,
    'Block Development Officer, Amritsar-II',
    'c0000001-0001-0257-0000-000000000027'
);

-- SDO Baba Bakala (258)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0258-0000-000000000027',
    'Sh. Jagdeep Singh PCS',
    '9800300040',
    'sdo.bababakala@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 27, 258,
    'Sub-Divisional Officer, Baba Bakala',
    'b0000001-0001-0000-0000-000000000027'
);

-- BDO Baba Bakala (258)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0258-0001-000000000027',
    'Sh. Harjit Kaur',
    '9800300041',
    'bdo.bababakala@punjab.gov.in',
    'ADMIN_BLOCK', 3, 27, 258, 258,
    'Block Development Officer, Baba Bakala',
    'c0000001-0001-0258-0000-000000000027'
);

-- SDO Lopoke (7341)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-7341-0000-000000000027',
    'Sh. Gurmeet Singh PCS',
    '9800300050',
    'sdo.lopoke@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 27, 7341,
    'Sub-Divisional Officer, Lopoke',
    'b0000001-0001-0000-0000-000000000027'
);

-- BDO Lopoke (7341)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-7341-0001-000000000027',
    'Sh. Amarjit Singh',
    '9800300051',
    'bdo.lopoke@punjab.gov.in',
    'ADMIN_BLOCK', 3, 27, 7341, 7341,
    'Block Development Officer, Lopoke',
    'c0000001-0001-7341-0000-000000000027'
);

-- SDO Majitha (6854)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-6854-0000-000000000027',
    'Sh. Balwant Singh PCS',
    '9800300060',
    'sdo.majitha@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 27, 6854,
    'Sub-Divisional Officer, Majitha',
    'b0000001-0001-0000-0000-000000000027'
);

-- BDO Majitha (6854)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-6854-0001-000000000027',
    'Sh. Kulwinder Kaur',
    '9800300061',
    'bdo.majitha@punjab.gov.in',
    'ADMIN_BLOCK', 3, 27, 6854, 6854,
    'Block Development Officer, Majitha',
    'c0000001-0001-6854-0000-000000000027'
);

-- ═══════════════════════════════════════════════════════════════
-- BATHINDA (District Code: 28)
-- Subdistricts: Bathinda(245), Maur(6858),
--               Rampura Phul(244), Talwandi Sabo(246)
-- ═══════════════════════════════════════════════════════════════

-- DM Bathinda
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, designation, created_by)
VALUES (
    'b0000001-0001-0000-0000-000000000028',
    'Sh. Showkat Ahmad Parre IAS',
    '9800400001',
    'dm.bathinda@punjab.gov.in',
    'ADMIN_DISTRICT', 3, 28,
    'Deputy Commissioner, Bathinda',
    'a0000001-0000-0000-0000-000000000001'
);

-- SDO Bathinda (245)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0245-0000-000000000028',
    'Sh. Harwinder Kaur PCS',
    '9800400010',
    'sdo.bathinda@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 28, 245,
    'Sub-Divisional Officer, Bathinda',
    'b0000001-0001-0000-0000-000000000028'
);

-- BDO Bathinda (245)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0245-0001-000000000028',
    'Sh. Satnam Singh',
    '9800400011',
    'bdo.bathinda@punjab.gov.in',
    'ADMIN_BLOCK', 3, 28, 245, 245,
    'Block Development Officer, Bathinda',
    'c0000001-0001-0245-0000-000000000028'
);

-- SDO Maur (6858)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-6858-0000-000000000028',
    'Sh. Davinder Singh PCS',
    '9800400020',
    'sdo.maur@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 28, 6858,
    'Sub-Divisional Officer, Maur',
    'b0000001-0001-0000-0000-000000000028'
);

-- BDO Maur (6858)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-6858-0001-000000000028',
    'Sh. Jaswinder Kaur',
    '9800400021',
    'bdo.maur@punjab.gov.in',
    'ADMIN_BLOCK', 3, 28, 6858, 6858,
    'Block Development Officer, Maur',
    'c0000001-0001-6858-0000-000000000028'
);

-- SDO Rampura Phul (244)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0244-0000-000000000028',
    'Sh. Tarsem Lal PCS',
    '9800400030',
    'sdo.rampuraphul@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 28, 244,
    'Sub-Divisional Officer, Rampura Phul',
    'b0000001-0001-0000-0000-000000000028'
);

-- BDO Rampura Phul (244)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0244-0001-000000000028',
    'Sh. Bhagwant Kaur',
    '9800400031',
    'bdo.rampuraphul@punjab.gov.in',
    'ADMIN_BLOCK', 3, 28, 244, 244,
    'Block Development Officer, Rampura Phul',
    'c0000001-0001-0244-0000-000000000028'
);

-- SDO Talwandi Sabo (246)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, designation, created_by)
VALUES (
    'c0000001-0001-0246-0000-000000000028',
    'Sh. Rupinder Singh PCS',
    '9800400040',
    'sdo.talwandisabo@punjab.gov.in',
    'ADMIN_SUBDIVISION', 3, 28, 246,
    'Sub-Divisional Officer, Talwandi Sabo',
    'b0000001-0001-0000-0000-000000000028'
);

-- BDO Talwandi Sabo (246)
INSERT INTO officers (id, name, phone_no, email, role, state_code, district_code, subdistrict_code, block_code, designation, created_by)
VALUES (
    'd0000001-0001-0246-0001-000000000028',
    'Sh. Lakhwinder Singh',
    '9800400041',
    'bdo.talwandisabo@punjab.gov.in',
    'ADMIN_BLOCK', 3, 28, 246, 246,
    'Block Development Officer, Talwandi Sabo',
    'c0000001-0001-0246-0000-000000000028'
);
