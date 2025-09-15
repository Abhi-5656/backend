-- V22__create_leave_balance_table.sql

CREATE TABLE employee_leave_balances (
                                         id BIGSERIAL PRIMARY KEY,
                                         employee_id BIGINT NOT NULL,
                                         leave_policy_id BIGINT NOT NULL,
                                         balance NUMERIC(10, 2) NOT NULL,

                                         CONSTRAINT fk_leave_balance_employee
                                             FOREIGN KEY (employee_id) REFERENCES employees(id)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT fk_leave_balance_leave_policy
                                             FOREIGN KEY (leave_policy_id) REFERENCES leave_policy(id)
                                                 ON DELETE CASCADE
);

CREATE INDEX idx_leave_balance_employee_id ON employee_leave_balances(employee_id);
CREATE INDEX idx_leave_balance_leave_policy_id ON employee_leave_balances(leave_policy_id);