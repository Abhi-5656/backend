-- -- V22__create_leave_balance_table.sql
--
-- CREATE TABLE employee_leave_balances (
--                                          id BIGSERIAL PRIMARY KEY,
--                                          employee_id VARCHAR(50) NOT NULL,
--                                          leave_policy_id BIGINT NOT NULL,
--                                          balance NUMERIC(10, 2) NOT NULL,
--                                          effective_date DATE NOT NULL,
--                                          expiration_date DATE,
--                                          last_accrual_date DATE, -- <-- ADDED THIS LINE
--
--                                          CONSTRAINT fk_leave_balance_employee
--                                              FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
--                                                  ON DELETE CASCADE,
--
--                                          CONSTRAINT fk_leave_balance_leave_policy
--                                              FOREIGN KEY (leave_policy_id) REFERENCES leave_policy(id)
--                                                  ON DELETE CASCADE
-- );
--
-- CREATE INDEX idx_leave_balance_employee_id ON employee_leave_balances(employee_id);
-- CREATE INDEX idx_leave_balance_leave_policy_id ON employee_leave_balances(leave_policy_id);
--
--
--
-- CREATE TABLE leave_requests (
--                                 id BIGSERIAL PRIMARY KEY,
--                                 employee_id VARCHAR(50) NOT NULL,
--                                 leave_policy_id BIGINT NOT NULL,
--                                 start_date DATE NOT NULL,
--                                 end_date DATE NOT NULL,
--                                 leave_days NUMERIC(10, 2) NOT NULL,
--                                 reason TEXT,
--                                 status VARCHAR(255) NOT NULL,
--
--                                 CONSTRAINT fk_leave_request_employee
--                                     FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
--                                         ON DELETE CASCADE,
--
--                                 CONSTRAINT fk_leave_request_leave_policy
--                                     FOREIGN KEY (leave_policy_id) REFERENCES leave_policy(id)
--                                         ON DELETE CASCADE
-- );
--
-- CREATE TABLE leave_request_approvals (
--                                          id BIGSERIAL PRIMARY KEY,
--                                          leave_request_id BIGINT NOT NULL,
--                                          approver_id VARCHAR(50) NOT NULL,
--                                          approval_level INT NOT NULL,
--                                          status VARCHAR(255) NOT NULL,
--
--                                          CONSTRAINT fk_approval_leave_request
--                                              FOREIGN KEY (leave_request_id) REFERENCES leave_requests(id)
--                                                  ON DELETE CASCADE,
--
--                                          CONSTRAINT fk_approval_approver
--                                              FOREIGN KEY (approver_id) REFERENCES employees(employee_id)
--                                                  ON DELETE CASCADE
-- );


-- V22__create_leave_balance_table.sql
-- Creates the leave balance, leave request, and approval tables.
-- The 'employee_leave_balances' table includes all tracking fields.

CREATE TABLE employee_leave_balances (
                                         id BIGSERIAL PRIMARY KEY,
                                         employee_id VARCHAR(50) NOT NULL,
                                         leave_policy_id BIGINT NOT NULL,

    -- RENAMED and NEW Tracking Fields --
                                         current_balance NUMERIC(10, 2) NOT NULL DEFAULT 0.0,
                                         total_granted NUMERIC(10, 2) NOT NULL DEFAULT 0.0,
                                         used_balance NUMERIC(10, 2) NOT NULL DEFAULT 0.0,

    -- Date Fields --
                                         effective_date DATE NOT NULL,
                                         expiration_date DATE,
                                         last_accrual_date DATE,
                                         next_accrual_date DATE,

    -- Status Field --
                                         status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    -- Timestamp Fields --
                                         created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                         updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

                                         CONSTRAINT fk_leave_balance_employee
                                             FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT fk_leave_balance_leave_policy
                                             FOREIGN KEY (leave_policy_id) REFERENCES leave_policy(id)
                                                 ON DELETE CASCADE
);

CREATE INDEX idx_leave_balance_employee_id ON employee_leave_balances(employee_id);
CREATE INDEX idx_leave_balance_leave_policy_id ON employee_leave_balances(leave_policy_id);


-- Leave Requests Table
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

-- Leave Request Approvals Table
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

-- Trigger Function for updated_at on employee_leave_balances
CREATE OR REPLACE FUNCTION update_leave_balance_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_leave_balance_updated_at ON employee_leave_balances;
CREATE TRIGGER trg_update_leave_balance_updated_at
    BEFORE UPDATE ON employee_leave_balances
    FOR EACH ROW
    EXECUTE FUNCTION update_leave_balance_updated_at();