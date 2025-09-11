-- V20__create_leave_profile_assignments_table.sql

CREATE TABLE leave_profile_assignments (
                                           id BIGSERIAL PRIMARY KEY,
                                           employee_id VARCHAR(50) NOT NULL,
                                           leave_profile_id BIGINT NOT NULL,
                                           assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                           effective_date DATE NOT NULL,
                                           expiration_date DATE,
                                           active BOOLEAN NOT NULL DEFAULT TRUE,

                                           CONSTRAINT fk_leave_profile_assignment_profile
                                               FOREIGN KEY (leave_profile_id) REFERENCES leave_profile(id)
                                                   ON DELETE CASCADE,

                                           CONSTRAINT fk_leave_profile_assignment_employee
                                               FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
                                                   ON DELETE CASCADE
);

CREATE INDEX idx_leave_profile_assignment_employee_id ON leave_profile_assignments(employee_id);
CREATE INDEX idx_leave_profile_assignment_profile_id ON leave_profile_assignments(leave_profile_id);