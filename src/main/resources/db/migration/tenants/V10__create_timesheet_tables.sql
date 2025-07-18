-- ================================
-- Table: timesheets
-- ================================

CREATE TABLE timesheets (
                            id BIGSERIAL PRIMARY KEY,
                            employee_id VARCHAR(64) NOT NULL,
                            work_date DATE NOT NULL,
                            regular_hours_minutes INTEGER,
                            excess_hours_minutes INTEGER,
                            total_work_duration_minutes INTEGER,
                            status VARCHAR(32),
                            rule_results_json TEXT,                    -- for PayPolicyRuleResultDTO JSON
                            calculated_at DATE,                        -- when recalculated (date only)
                            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                            updated_at TIMESTAMP,
                            CONSTRAINT uc_employee_work_date UNIQUE (employee_id, work_date)
);

CREATE INDEX idx_timesheets_employee_date ON timesheets (employee_id, work_date);

-- ================================
-- Table: punch_events
-- ================================

CREATE TABLE punch_events (
                              id BIGSERIAL PRIMARY KEY,
                              employee_id VARCHAR(64) NOT NULL,
                              event_time TIMESTAMP NOT NULL,
                              punch_type VARCHAR(16) NOT NULL,
                              status VARCHAR(16) NOT NULL,
                              device_id VARCHAR(64),
                              geo_lat DOUBLE PRECISION,
                              geo_long DOUBLE PRECISION,
                              notes VARCHAR(255),
                              employee_image_base64 TEXT, -- <<<< ADDED THIS LINE
                              timesheet_id BIGINT,
                              shift_id BIGINT,
                              exception_flag BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMP,

                              CONSTRAINT fk_punch_events_timesheet
                                  FOREIGN KEY (timesheet_id)
                                      REFERENCES timesheets (id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_punch_events_shift
                                  FOREIGN KEY (shift_id)
                                      REFERENCES shifts (id)
                                      ON DELETE SET NULL,

                              CONSTRAINT uc_employee_event_time UNIQUE (employee_id, event_time)
);

CREATE INDEX idx_punch_events_employee_time ON punch_events (employee_id, event_time);
CREATE INDEX idx_punch_events_timesheet ON punch_events (timesheet_id);
CREATE INDEX idx_punch_events_shift_id ON punch_events (shift_id);