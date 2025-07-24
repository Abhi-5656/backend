-- V15__create_leave_policy_and_leave_profile_tables.sql

-- ======================================
-- 1) leave_policy table
-- ======================================
CREATE TABLE IF NOT EXISTS leave_policy (
                                            id                          BIGSERIAL PRIMARY KEY,
                                            leave_name                  VARCHAR(100)   NOT NULL,
    code                        VARCHAR(50)    UNIQUE,
    effective_date              DATE           NOT NULL,
    expiration_date             DATE,

    profile_select              VARCHAR(20)    NOT NULL,
    calendar_color              VARCHAR(20),
    enable_leave_config         BOOLEAN        NOT NULL,

    measure_by                  VARCHAR(10)    NOT NULL,
    paid_unpaid                 VARCHAR(10)    NOT NULL,

    full_day                    BOOLEAN        NOT NULL,
    half_day                    BOOLEAN        NOT NULL,

    max_days_year               INTEGER,
    max_days_month              INTEGER,
    max_consecutive_days        INTEGER,

    min_advance_notice          INTEGER,
    min_worked                  INTEGER,
    occurrence_limit            INTEGER,

    occurrence_period           VARCHAR(12)    NOT NULL,
    enable_carryover_proration  BOOLEAN        NOT NULL,
    allow_carryover             BOOLEAN        NOT NULL,
    carryover_cap               INTEGER,

    calculation_basis           VARCHAR(20)    NOT NULL,
    auto_encash                 BOOLEAN        NOT NULL,

    allow_proration             BOOLEAN        NOT NULL,
    proration_mode              VARCHAR(10)    NOT NULL,
    join_date_threshold         INTEGER,

    rounding                    VARCHAR(10)    NOT NULL,

    enable_attachments          BOOLEAN        NOT NULL,
    attachment_required         BOOLEAN        NOT NULL,

    pdf                         BOOLEAN        NOT NULL,
    jpg                         BOOLEAN        NOT NULL,
    png                         BOOLEAN        NOT NULL,
    docx                        BOOLEAN        NOT NULL,

    created_at                  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    -- enforce Java enum values
    CONSTRAINT chk_profile_select     CHECK (profile_select  IN ('FULL_TIME','PROBATIONARY','PART_TIMER','INTERN','CONTRACT')),
    CONSTRAINT chk_measure_by         CHECK (measure_by      IN ('DAYS','HOURS')),
    CONSTRAINT chk_paid_unpaid        CHECK (paid_unpaid     IN ('PAID','UNPAID')),
    CONSTRAINT chk_occurrence_period  CHECK (occurrence_period IN ('YEARLY','PAY_PERIOD')),
    CONSTRAINT chk_calculation_basis  CHECK (calculation_basis IN ('CALENDAR_YEAR','ANNIVERSARY_YEAR')),
    CONSTRAINT chk_proration_mode     CHECK (proration_mode  IN ('MONTHLY','YEARLY')),
    CONSTRAINT chk_rounding           CHECK (rounding        IN ('NEAREST','UP','DOWN'))
    );

-- ======================================
-- 2) conditional_rule table
-- ======================================
CREATE TABLE IF NOT EXISTS conditional_rule (
                                                id                          BIGSERIAL PRIMARY KEY,
                                                tenure                      INTEGER,
                                                override_max                INTEGER,
                                                override_min_notice         INTEGER,
                                                override_min_worked         INTEGER,
                                                override_occurrence_limit   INTEGER,
                                                override_occurrence_period  VARCHAR(12),

    leave_policy_id             BIGINT  NOT NULL
    REFERENCES leave_policy(id)
    ON DELETE CASCADE,

    created_at                  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    -- enforce OccurrencePeriod enum
    CONSTRAINT chk_override_occurrence_period
    CHECK (override_occurrence_period IN ('YEARLY','PAY_PERIOD'))
    );

-- ======================================
-- 3) leave_profile table
-- ======================================
CREATE TABLE IF NOT EXISTS leave_profile (
                                             id              BIGSERIAL      PRIMARY KEY,
                                             profile_name    VARCHAR(100)   NOT NULL UNIQUE,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
    );

-- ======================================
-- 4) join table: leave_profile ↔ leave_policy
-- ======================================
CREATE TABLE IF NOT EXISTS leave_profile_leave_policy (
                                                          leave_profile_id BIGINT NOT NULL,
                                                          leave_policy_id  BIGINT NOT NULL,
                                                          PRIMARY KEY (leave_profile_id, leave_policy_id),
    FOREIGN KEY (leave_profile_id)
    REFERENCES leave_profile(id)
    ON DELETE CASCADE,
    FOREIGN KEY (leave_policy_id)
    REFERENCES leave_policy(id)
    ON DELETE CASCADE
    );

-- ======================================
-- 5) trigger function to auto‐update updated_at
-- ======================================
CREATE OR REPLACE FUNCTION trg_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at := NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop existing triggers if any, then attach to each table:

-- leave_policy
DROP TRIGGER IF EXISTS set_updated_at ON leave_policy;
CREATE TRIGGER set_updated_at
    BEFORE UPDATE ON leave_policy
    FOR EACH ROW
    EXECUTE FUNCTION trg_set_updated_at();

-- conditional_rule
DROP TRIGGER IF EXISTS set_updated_at ON conditional_rule;
CREATE TRIGGER set_updated_at
    BEFORE UPDATE ON conditional_rule
    FOR EACH ROW
    EXECUTE FUNCTION trg_set_updated_at();

-- leave_profile
DROP TRIGGER IF EXISTS set_updated_at ON leave_profile;
CREATE TRIGGER set_updated_at
    BEFORE UPDATE ON leave_profile
    FOR EACH ROW
    EXECUTE FUNCTION trg_set_updated_at();

-- leave_profile_leave_policy does not need updated_at
