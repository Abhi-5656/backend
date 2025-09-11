-- V21__create_request_type_assignments_table.sql

CREATE TABLE request_type_assignments (
                                          id BIGSERIAL PRIMARY KEY,
                                          employee_id VARCHAR(50) NOT NULL,
                                          request_type_id BIGINT NOT NULL,
                                          assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                          effective_date DATE NOT NULL,
                                          expiration_date DATE,
                                          active BOOLEAN NOT NULL DEFAULT TRUE,

                                          CONSTRAINT fk_request_type_assignment_request_type
                                              FOREIGN KEY (request_type_id) REFERENCES request_types(id)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT fk_request_type_assignment_employee
                                              FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
                                                  ON DELETE CASCADE
);

CREATE INDEX idx_request_type_assignment_employee_id ON request_type_assignments(employee_id);
CREATE INDEX idx_request_type_assignment_request_type_id ON request_type_assignments(request_type_id);