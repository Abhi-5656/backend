-- V11__create_pay_policy_assignments_table.sql

CREATE TABLE pay_policy_assignments (
                                        id BIGSERIAL PRIMARY KEY,
                                        employee_id VARCHAR(50) NOT NULL,
                                        pay_policy_id BIGINT NOT NULL,
                                        assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                        effective_date DATE NOT NULL,
                                        expiration_date DATE,
                                        active BOOLEAN NOT NULL DEFAULT TRUE,

                                        CONSTRAINT fk_pay_policy
                                            FOREIGN KEY (pay_policy_id) REFERENCES pay_policies(id)
                                                ON DELETE CASCADE,

    -- Only add this if you have an 'employees' table with employee_id (VARCHAR) as PK
                                        CONSTRAINT fk_employee
                                            FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
                                                ON DELETE CASCADE
);

CREATE INDEX idx_pay_policy_assignment_employee_id ON pay_policy_assignments(employee_id);
CREATE INDEX idx_pay_policy_assignment_policy_id ON pay_policy_assignments(pay_policy_id);
