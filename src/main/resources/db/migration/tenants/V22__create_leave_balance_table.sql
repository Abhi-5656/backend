-- harshwfm/wfm-backend/HarshWfm-wfm-backend-573b561b9a0299c8388f2f15252dbc2875a7884a/src/main/resources/db/migration/tenants/V22__create_leave_balance_table.sql
-- V22__create_leave_balance_table.sql

CREATE TABLE employee_leave_balances (
                                         id BIGSERIAL PRIMARY KEY,
                                         employee_id VARCHAR(50) NOT NULL,
                                         leave_policy_id BIGINT NOT NULL,
                                         balance NUMERIC(10, 2) NOT NULL,
                                         effective_date DATE NOT NULL,
                                         expiration_date DATE,

                                         CONSTRAINT fk_leave_balance_employee
                                             FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT fk_leave_balance_leave_policy
                                             FOREIGN KEY (leave_policy_id) REFERENCES leave_policy(id)
                                                 ON DELETE CASCADE
);

CREATE INDEX idx_leave_balance_employee_id ON employee_leave_balances(employee_id);
CREATE INDEX idx_leave_balance_leave_policy_id ON employee_leave_balances(leave_policy_id);



CREATE TABLE leave_requests (
                                id BIGSERIAL PRIMARY KEY,
                                employee_id VARCHAR(50) NOT NULL,
                                leave_policy_id BIGINT NOT NULL,
                                start_date DATE NOT NULL,
                                end_date DATE NOT NULL,
                                leave_days NUMERIC(10, 2) NOT NULL,
                                reason TEXT,
                                status VARCHAR(255) NOT NULL,

                                CONSTRAINT fk_leave_request_employee
                                    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_leave_request_leave_policy
                                    FOREIGN KEY (leave_policy_id) REFERENCES leave_policy(id)
                                        ON DELETE CASCADE
);

CREATE TABLE leave_request_approvals (
                                         id BIGSERIAL PRIMARY KEY,
                                         leave_request_id BIGINT NOT NULL,
                                         approver_id VARCHAR(50) NOT NULL,
                                         approval_level INT NOT NULL,
                                         status VARCHAR(255) NOT NULL,

                                         CONSTRAINT fk_approval_leave_request
                                             FOREIGN KEY (leave_request_id) REFERENCES leave_requests(id)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT fk_approval_approver
                                             FOREIGN KEY (approver_id) REFERENCES employees(employee_id)
                                                 ON DELETE CASCADE
);