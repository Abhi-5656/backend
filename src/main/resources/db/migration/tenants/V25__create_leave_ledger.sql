-- V25__create_leave_ledger.sql
-- Creates the new table for storing every leave transaction (grant, use, adjustment).

-- 1. Create an ENUM type for the transaction types
-- (REMOVED: The 'CREATE TYPE leave_transaction_type ...' block is now gone)

-- 2. Create the new leave ledger table
CREATE TABLE employee_leave_ledger (
                                       id BIGSERIAL PRIMARY KEY,
                                       employee_id VARCHAR(50) NOT NULL,
                                       leave_policy_id BIGINT NOT NULL,
                                       transaction_type VARCHAR(50) NOT NULL, -- CHANGED from 'leave_transaction_type' to 'VARCHAR(50)'
                                       amount NUMERIC(10, 2) NOT NULL, -- Positive for grant, negative for usage
                                       transaction_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                       notes TEXT,
                                       related_request_id BIGINT, -- To link to a leave_requests.id
                                       created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                                       CONSTRAINT fk_ledger_employee
                                           FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
                                               ON DELETE CASCADE,
                                       CONSTRAINT fk_ledger_leave_policy
                                           FOREIGN KEY (leave_policy_id) REFERENCES leave_policy(id)
                                               ON DELETE CASCADE,
                                       CONSTRAINT fk_ledger_leave_request
                                           FOREIGN KEY (related_request_id) REFERENCES leave_requests(id)
                                               ON DELETE SET NULL
);

-- 3. Add indexes for faster queries
CREATE INDEX idx_ledger_employee_id ON employee_leave_ledger(employee_id);
CREATE INDEX idx_ledger_leave_policy_id ON employee_leave_ledger(leave_policy_id);
CREATE INDEX idx_ledger_employee_policy ON employee_leave_ledger(employee_id, leave_policy_id);