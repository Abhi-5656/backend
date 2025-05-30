-- ================================
-- Table: timesheets
-- ================================
CREATE TABLE timesheets (
                            id BIGSERIAL PRIMARY KEY,
                            employee_id BIGINT NOT NULL,
                            work_date DATE NOT NULL,
                            total_work_duration DOUBLE PRECISION,
                            overtime_duration DOUBLE PRECISION,
                            status VARCHAR(32),
                            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                            updated_at TIMESTAMP
);

CREATE INDEX idx_timesheets_employee_date ON timesheets (employee_id, work_date);

-- ================================
-- Table: punch_events
-- ================================
CREATE TABLE punch_events (
                              id BIGSERIAL PRIMARY KEY,
                              employee_id BIGINT NOT NULL,
                              event_time TIMESTAMP NOT NULL,
                              punch_type VARCHAR(16) NOT NULL,
                              status VARCHAR(16) NOT NULL,
                              device_id VARCHAR(64),
                              geo_lat DOUBLE PRECISION,
                              geo_long DOUBLE PRECISION,
                              notes VARCHAR(255),
                              timesheet_id BIGINT,
                              shift_id BIGINT, -- NEW COLUMN
                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMP,
                              CONSTRAINT fk_punch_events_timesheet
                                  FOREIGN KEY (timesheet_id)
                                      REFERENCES timesheets (id)
                                      ON DELETE CASCADE,
                              CONSTRAINT fk_punch_events_shift
                                  FOREIGN KEY (shift_id)
                                      REFERENCES shifts(id)
                                      ON DELETE SET NULL
);

CREATE INDEX idx_punch_events_employee_time ON punch_events (employee_id, event_time);
CREATE INDEX idx_punch_events_timesheet ON punch_events (timesheet_id);
CREATE INDEX idx_punch_events_shift_id ON punch_events (shift_id);

-- ================================
-- Optional: Unique constraint to prevent duplicate punches for an employee at the same time
-- ================================
-- ALTER TABLE punch_events
--     ADD CONSTRAINT uc_employee_event_time UNIQUE (employee_id, event_time);
