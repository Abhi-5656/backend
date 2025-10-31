-- Flyway migration script for creating the entire leave policy schema

-- Drop existing tables if they exist to start fresh for this migration
DROP TABLE IF EXISTS leave_policy_applicability, leave_policy, lp_limits_config, lp_grants_config, lp_attachments_config, lp_calculation_date_config, lp_eligibility_config, lp_encashment_config, lp_carry_forward_config, lp_earned_grant_config, lp_fixed_grant_config, lp_repeatedly_grant_details, lp_one_time_grant_details, lp_proration_config, lp_allowed_files, lp_accrual_earning_limits_config, lp_carry_forward_limits_config, lp_encashment_limits_config CASCADE;

-- Section 1: Create the most granular configuration tables first.
CREATE TABLE lp_proration_config (
                                     id BIGSERIAL PRIMARY KEY,
                                     is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                     cutoff_unit VARCHAR(255),
                                     cutoff_value INT,
                                     grant_percentage_before_cutoff INT,
                                     grant_percentage_after_cutoff INT
);

CREATE TABLE lp_one_time_grant_details (
                                           id BIGSERIAL PRIMARY KEY,
                                           max_days INT,
                                           min_advance_notice_in_days INT,
                                           min_worked_before_grant_in_days INT
);

CREATE TABLE lp_repeatedly_grant_details (
                                             id BIGSERIAL PRIMARY KEY,
                                             max_days_per_year NUMERIC(10, 2),
                                             max_days_per_month NUMERIC(10, 2),
                                             max_days_per_pay_period NUMERIC(10, 2),
                                             grant_period VARCHAR(255),
                                             posting VARCHAR(255),
                                             min_advance_notice_in_days INT,
                                             min_worked_before_grant_in_days INT,
                                             proration_config_id BIGINT UNIQUE REFERENCES lp_proration_config(id)
);

CREATE TABLE lp_earned_grant_config (
                                        id BIGSERIAL PRIMARY KEY,
                                        max_days_per_year NUMERIC(10, 2),
                                        max_days_per_month NUMERIC(10, 2),
                                        max_days_per_pay_period NUMERIC(10, 2),
                                        rate_per_period DOUBLE PRECISION,
                                        max_consecutive_days INT,
                                        grant_period VARCHAR(255),
                                        posting VARCHAR(255),
                                        min_advance_notice_in_days INT,
                                        proration_config_id BIGINT UNIQUE REFERENCES lp_proration_config(id)
);

-- Old granular tables (lp_carry_forward_config, lp_encashment_config, lp_eligibility_config) are REMOVED.

CREATE TABLE lp_calculation_date_config (
                                            id BIGSERIAL PRIMARY KEY,
                                            is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                            calculation_type VARCHAR(255),
                                            custom_date DATE
);

CREATE TABLE lp_attachments_config (
                                       id BIGSERIAL PRIMARY KEY
);

-- This is a join table for the ElementCollection in AttachmentsConfig
CREATE TABLE lp_allowed_files (
                                  attachments_config_id BIGINT NOT NULL REFERENCES lp_attachments_config(id) ON DELETE CASCADE,
                                  file_type VARCHAR(255)
);


-- Section 2: Create composite configuration tables that link to the granular tables.

CREATE TABLE lp_fixed_grant_config (
                                       id BIGSERIAL PRIMARY KEY,
                                       frequency VARCHAR(255),
                                       one_time_details_id BIGINT UNIQUE REFERENCES lp_one_time_grant_details(id),
                                       repeatedly_details_id BIGINT UNIQUE REFERENCES lp_repeatedly_grant_details(id)
);

CREATE TABLE lp_grants_config (
                                  id BIGSERIAL PRIMARY KEY,
                                  is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                  grant_type VARCHAR(255),
                                  expiration VARCHAR(255),
                                  fixed_grant_id BIGINT UNIQUE REFERENCES lp_fixed_grant_config(id),
                                  earned_grant_id BIGINT UNIQUE REFERENCES lp_earned_grant_config(id)
);

-- --- NEW LIMITS STRUCTURE ---
-- NEW Table for Accrual Earning Limits (Name Updated)
CREATE TABLE lp_accrual_earning_limits_config (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                                  max_balance_cap INT
);

-- NEW Table for Carry Forward Limits (properties are merged)
CREATE TABLE lp_carry_forward_limits_config (
                                                id BIGSERIAL PRIMARY KEY,
                                                is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                                cap INT,
                                                cap_type VARCHAR(255)
);

-- NEW Table for Encashment Limits (properties are merged)
CREATE TABLE lp_encashment_limits_config (
                                             id BIGSERIAL PRIMARY KEY,
                                             is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                             max_encashable_days INT
);

-- UPDATED: lp_limits_config now links to the three new tables above
CREATE TABLE lp_limits_config (
                                  id BIGSERIAL PRIMARY KEY,
                                  is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                  accrual_earning_limits_config_id BIGINT UNIQUE REFERENCES lp_accrual_earning_limits_config(id), -- Name Updated
                                  carry_forward_limits_config_id BIGINT UNIQUE REFERENCES lp_carry_forward_limits_config(id),
                                  encashment_limits_config_id BIGINT UNIQUE REFERENCES lp_encashment_limits_config(id)
);
-- --- END NEW LIMITS STRUCTURE ---


-- Section 3: Create the main leave_policy table that ties everything together.

CREATE TABLE leave_policy (
                              id BIGSERIAL PRIMARY KEY,
                              policy_name VARCHAR(100) NOT NULL UNIQUE,
                              leave_code VARCHAR(20) UNIQUE,
                              leave_type VARCHAR(255) NOT NULL,
                              leave_color VARCHAR(255) NOT NULL,
                              calculation_date_config_id BIGINT UNIQUE REFERENCES lp_calculation_date_config(id),
                              grants_config_id BIGINT UNIQUE REFERENCES lp_grants_config(id),
                              limits_config_id BIGINT UNIQUE REFERENCES lp_limits_config(id),
                              attachments_config_id BIGINT UNIQUE REFERENCES lp_attachments_config(id)
);


-- Section 4: Create the final join table for the ElementCollection in LeavePolicy.

CREATE TABLE leave_policy_applicability (
                                            policy_id BIGINT NOT NULL REFERENCES leave_policy(id) ON DELETE CASCADE,
                                            applicability VARCHAR(255) NOT NULL
);