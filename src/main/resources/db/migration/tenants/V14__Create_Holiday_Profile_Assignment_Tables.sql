-- V14__create_holiday_profile_assignments_table.sql

CREATE TABLE holiday_profile_assignments (
                                             id BIGSERIAL PRIMARY KEY,
                                             holiday_profile_id BIGINT NOT NULL,
                                             effective_date DATE NOT NULL,
                                             expiration_date DATE,
                                             is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                             employee_id VARCHAR(255) NOT NULL,
                                             CONSTRAINT fk_holiday_profile
                                                 FOREIGN KEY (holiday_profile_id)
                                                     REFERENCES holiday_profiles(id)
                                                     ON DELETE CASCADE
);

-- index to speed up lookups by employee
CREATE INDEX idx_hpa_employee_id
    ON holiday_profile_assignments(employee_id);

-- (optional) index on holiday_profile_id for faster joins
CREATE INDEX idx_hpa_holiday_profile_id
    ON holiday_profile_assignments(holiday_profile_id);
