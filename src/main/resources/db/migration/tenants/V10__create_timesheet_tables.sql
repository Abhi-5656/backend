-- ================================
-- Table: timesheets
-- ================================
CREATE TABLE timesheets (
                            id BIGSERIAL PRIMARY KEY,
                            employee_id VARCHAR(64) NOT NULL,
                            work_date DATE NOT NULL,
                            work_duration_minutes INTEGER,             -- total minutes worked in day
                            total_work_duration DOUBLE PRECISION,      -- hours, e.g., 7.5
                            overtime_duration INTEGER,                 -- overtime in minutes
                            status VARCHAR(32),
                            rule_results_json TEXT,                    -- for PayPolicyRuleResultDTO JSON
                            calculated_at DATE,                        -- when recalculated (date only)
                            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                            updated_at TIMESTAMP,
    -- Ensures only one timesheet record per employee per day
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
    -- Ensures an employee cannot have the same punch type at the exact same timestamp,
    -- but allows different punch types at the same timestamp.
                              CONSTRAINT uc_employee_event_time_type UNIQUE (employee_id, event_time, punch_type)
);

CREATE INDEX idx_punch_events_employee_time ON punch_events (employee_id, event_time);
CREATE INDEX idx_punch_events_timesheet ON punch_events (timesheet_id);
CREATE INDEX idx_punch_events_shift_id ON punch_events (shift_id);
