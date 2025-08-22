-- Flyway migration script for creating the entire leave policy schema

-- Section 1: Create the most granular configuration tables first.
-- These tables have no dependencies on other tables in this schema.

CREATE TABLE lp_one_time_grant_details (
                                           id BIGSERIAL PRIMARY KEY,
                                           max_days INT,
                                           min_advance_notice_in_days INT,
                                           min_worked_before_grant_in_days INT
);

CREATE TABLE lp_repeatedly_grant_details (
                                             id BIGSERIAL PRIMARY KEY,
                                             max_days_per_year INT,
                                             max_days_per_month INT,
                                             min_advance_notice_in_days INT,
                                             min_worked_before_grant_in_days INT
);

CREATE TABLE lp_earned_grant_config (
                                        id BIGSERIAL PRIMARY KEY,
                                        max_days_per_year INT,
                                        rate_per_period DOUBLE PRECISION,
                                        max_consecutive_days INT,
                                        accrual_cadence VARCHAR(255),
                                        posting VARCHAR(255),
                                        min_advance_notice_in_days INT
);

CREATE TABLE lp_carry_forward_config (
                                         id BIGSERIAL PRIMARY KEY,
                                         cap INT,
                                         cap_type VARCHAR(255),
                                         expiry_in_days INT
);

CREATE TABLE lp_encashment_config (
                                      id BIGSERIAL PRIMARY KEY,
                                      max_encashable_days INT
);

CREATE TABLE lp_eligibility_config (
                                       id BIGSERIAL PRIMARY KEY,
                                       gender VARCHAR(255)
);

CREATE TABLE lp_calculation_date_config (
                                            id BIGSERIAL PRIMARY KEY,
                                            is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                            calculation_type VARCHAR(255),
                                            custom_date DATE
);

CREATE TABLE lp_attachments_config (
                                       id BIGSERIAL PRIMARY KEY,
                                       is_enabled BOOLEAN NOT NULL DEFAULT FALSE
);

-- This is a join table for the ElementCollection in AttachmentsConfig
CREATE TABLE lp_allowed_files (
                                  attachments_config_id BIGINT NOT NULL REFERENCES lp_attachments_config(id),
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

CREATE TABLE lp_limits_config (
                                  id BIGSERIAL PRIMARY KEY,
                                  is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                  carry_forward_config_id BIGINT UNIQUE REFERENCES lp_carry_forward_config(id),
                                  encashment_config_id BIGINT UNIQUE REFERENCES lp_encashment_config(id),
                                  eligibility_config_id BIGINT UNIQUE REFERENCES lp_eligibility_config(id)
);


-- Section 3: Create the main leave_policy table that ties everything together.

CREATE TABLE leave_policy (
                              id BIGSERIAL PRIMARY KEY,
                              policy_name VARCHAR(100) NOT NULL UNIQUE,
                              leave_code VARCHAR(20) UNIQUE,
                              effective_date DATE NOT NULL,
                              expiration_date DATE,
                              leave_type VARCHAR(255) NOT NULL,
                              leave_color VARCHAR(255) NOT NULL,
                              calculation_date_config_id BIGINT UNIQUE REFERENCES lp_calculation_date_config(id),
                              grants_config_id BIGINT UNIQUE REFERENCES lp_grants_config(id),
                              limits_config_id BIGINT UNIQUE REFERENCES lp_limits_config(id),
                              attachments_config_id BIGINT UNIQUE REFERENCES lp_attachments_config(id)
);


-- Section 4: Create the final join table for the ElementCollection in LeavePolicy.

CREATE TABLE leave_policy_applicability (
                                            policy_id BIGINT NOT NULL REFERENCES leave_policy(id),
                                            applicability VARCHAR(255) NOT NULL
);