-- V9__create_pay_policy_schema.sql

-- ======================================
-- 1. Breaks and Break Rules
-- ======================================
CREATE TABLE break_rules (
                             id BIGSERIAL PRIMARY KEY,
                             enabled BOOLEAN NOT NULL,
                             allow_multiple BOOLEAN NOT NULL
);

CREATE TABLE breaks (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(100),
                        duration INTEGER,
                        start_time VARCHAR(10),
                        end_time VARCHAR(10),
                        type VARCHAR(10), -- ADDED: To store PAID or UNPAID
                        break_rules_id BIGINT REFERENCES break_rules(id) ON DELETE CASCADE
);

-- ======================================
-- 2. Rounding Rule & Rounding Rules
-- ======================================
CREATE TABLE rounding_rule (
                               id BIGSERIAL PRIMARY KEY,
                               "interval" INTEGER,
                               type VARCHAR(10),
                               grace_period INTEGER,
                               apply_before_shift_minutes INTEGER,
                               apply_after_shift_minutes INTEGER
);

CREATE TABLE rounding_rules (
                                id BIGSERIAL PRIMARY KEY,
                                enabled BOOLEAN NOT NULL,
                                scope VARCHAR(20) NOT NULL,
                                clock_in_rule_id BIGINT REFERENCES rounding_rule(id) ON DELETE CASCADE,
                                clock_out_rule_id BIGINT REFERENCES rounding_rule(id) ON DELETE CASCADE
);

-- ======================================
-- 3. Punch Event Rules
-- ======================================
CREATE TABLE punch_event_rules (
                                   id BIGSERIAL PRIMARY KEY,
                                   enabled BOOLEAN NOT NULL,
                                   early_in INTEGER,
                                   late_in INTEGER,
                                   early_out INTEGER,
                                   late_out INTEGER,
                                   notify_on_punch_events BOOLEAN NOT NULL
);

-- ======================================
-- 4. Pre Shift Inclusion
-- ======================================
CREATE TABLE pre_shift_inclusion (
                                     id BIGSERIAL PRIMARY KEY,
                                     enabled BOOLEAN NOT NULL,
                                     from_value INTEGER,
                                     from_unit VARCHAR(10),
                                     up_to_value INTEGER,
                                     up_to_unit VARCHAR(10)
);

-- ======================================
-- 5. Overtime Rules (UPDATED)
-- ======================================
CREATE TABLE overtime_rules (
                                id BIGSERIAL PRIMARY KEY,
                                enabled BOOLEAN NOT NULL,
                                daily_ot_trigger VARCHAR(50), -- ADDED
                                threshold_hours INTEGER,
                                threshold_minutes INTEGER,
                                grace_period_after_shift_end INTEGER, -- ADDED
                                max_ot_per_day DOUBLE PRECISION,
                                enable_weekly_ot BOOLEAN, -- ADDED
                                weekly_threshold_hours INTEGER, -- ADDED
                                max_ot_per_week DOUBLE PRECISION,
                                weekly_ot_basis VARCHAR(50), -- ADDED
                                daily_weekly_ot_conflict VARCHAR(50), -- ADDED
                                conflict_resolution VARCHAR(20),
                                reset_ot_bucket_daily BOOLEAN NOT NULL,
                                reset_ot_bucket_weekly BOOLEAN NOT NULL,
                                weekly_reset_day VARCHAR(20), -- ADDED
                                reset_ot_bucket_on_pay_period BOOLEAN NOT NULL,
                                compensation_method VARCHAR(20),
                                paid_ot_multiplier DOUBLE PRECISION,
                                comp_off_days_per_ot INTEGER,
                                comp_off_hours_per_ot INTEGER,
                                max_comp_off_balance INTEGER,
                                max_comp_off_balance_basis VARCHAR(20),
                                comp_off_expiry_value INTEGER,
                                comp_off_expiry_unit VARCHAR(10),
                                encash_on_expiry BOOLEAN NOT NULL,
                                pre_shift_inclusion_id BIGINT REFERENCES pre_shift_inclusion(id) ON DELETE CASCADE
);

-- ======================================
-- 6. OvertimeRules ↔ Shifts mapping (Many-to-Many)
-- ======================================
CREATE TABLE overtime_rules_shifts (
                                       overtime_rules_id BIGINT REFERENCES overtime_rules(id) ON DELETE CASCADE,
                                       shift_id BIGINT REFERENCES shifts(id) ON DELETE CASCADE,
                                       PRIMARY KEY (overtime_rules_id, shift_id)
);

-- ======================================
-- 7. Pay Period Rules
-- ======================================
CREATE TABLE pay_period_rules (
                                  id BIGSERIAL PRIMARY KEY,
                                  enabled BOOLEAN NOT NULL,
                                  period_type VARCHAR(20),
                                  reference_date VARCHAR(10),
                                  week_start VARCHAR(10)
);

CREATE TABLE pay_period_semi_monthly_days (
                                              pay_period_rules_id BIGINT REFERENCES pay_period_rules(id) ON DELETE CASCADE,
                                              day INTEGER,
                                              PRIMARY KEY (pay_period_rules_id, day)
);

-- ======================================
-- 8. Holiday Pay Rules
-- ======================================
CREATE TABLE holiday_pay_rules (
                                   id BIGSERIAL PRIMARY KEY,
                                   enabled BOOLEAN NOT NULL,
                                   holiday_pay_type VARCHAR(20),
                                   pay_multiplier DOUBLE PRECISION,
                                   min_hours_for_comp_off INTEGER,
                                   max_comp_off_balance_basis VARCHAR(20),
                                   max_comp_off_balance INTEGER,
                                   comp_off_expiry_value INTEGER,
                                   comp_off_expiry_unit VARCHAR(10),
                                   encash_on_expiry BOOLEAN NOT NULL
);

-- ======================================
-- 9. Attendance Rule
-- ======================================
CREATE TABLE attendance_rules (
                                  id BIGSERIAL PRIMARY KEY,
                                  enabled BOOLEAN NOT NULL,
                                  full_day_hours INTEGER,
                                  full_day_minutes INTEGER,
                                  half_day_hours INTEGER,
                                  half_day_minutes INTEGER
);

-- ======================================
-- 10. Night Allowance Rules
-- ======================================
CREATE TABLE night_allowance_rules (
                                       id BIGSERIAL PRIMARY KEY,
                                       enabled BOOLEAN NOT NULL,
                                       start_time VARCHAR(10),
                                       end_time VARCHAR(10),
                                       pay_multiplier DOUBLE PRECISION
);

-- ======================================
-- 11. Weekend Pay Rules (NEW)
-- ======================================
CREATE TABLE weekend_pay_rules (
                                   id BIGSERIAL PRIMARY KEY,
                                   enabled BOOLEAN NOT NULL,
                                   weekend_pay_type VARCHAR(20),
                                   pay_multiplier DOUBLE PRECISION,
                                   min_hours_for_comp_off INTEGER,
                                   max_comp_off_balance_basis VARCHAR(20),
                                   max_comp_off_balance INTEGER,
                                   comp_off_expiry_value INTEGER,
                                   comp_off_expiry_unit VARCHAR(10),
                                   encash_on_expiry BOOLEAN NOT NULL
);

CREATE TABLE weekend_pay_rule_days (
                                       rule_id BIGINT NOT NULL REFERENCES weekend_pay_rules(id) ON DELETE CASCADE,
                                       weekend_day VARCHAR(20) NOT NULL,
                                       PRIMARY KEY (rule_id, weekend_day)
);


-- ======================================
-- 12. Pay Policy (core) (UPDATED)
-- ======================================
CREATE TABLE pay_policies (
                              id BIGSERIAL PRIMARY KEY,
                              policy_name VARCHAR(100) NOT NULL UNIQUE,
                              effective_date DATE NOT NULL,
                              expiration_date DATE,
                              use_filo_calculation BOOLEAN DEFAULT FALSE,
                              rounding_rules_id BIGINT REFERENCES rounding_rules(id) ON DELETE CASCADE,
                              punch_event_rules_id BIGINT REFERENCES punch_event_rules(id) ON DELETE CASCADE,
                              break_rules_id BIGINT REFERENCES break_rules(id) ON DELETE CASCADE,
                              overtime_rules_id BIGINT REFERENCES overtime_rules(id) ON DELETE CASCADE,
                              night_allowance_rules_id BIGINT REFERENCES night_allowance_rules(id) ON DELETE CASCADE,
                              pay_period_rules_id BIGINT REFERENCES pay_period_rules(id) ON DELETE CASCADE,
                              holiday_pay_rules_id BIGINT REFERENCES holiday_pay_rules(id) ON DELETE CASCADE,
                              weekend_pay_rules_id BIGINT REFERENCES weekend_pay_rules(id) ON DELETE CASCADE, -- ADDED
                              attendance_rule_id BIGINT REFERENCES attendance_rules(id) ON DELETE CASCADE
);
