CREATE TABLE IF NOT EXISTS tracking_session (
  session_id        BIGSERIAL PRIMARY KEY,
  employee_id       varchar(50) NOT NULL,
  clock_in_time     timestamptz NOT NULL,
  clock_out_time    timestamptz,
  status            varchar(10) NOT NULL CHECK (status IN ('OPEN','CLOSED')),
  path_geometry     geometry(LineStringM, 4326),
  total_points      int NOT NULL DEFAULT 0,
  total_distance_m  numeric(12,2) DEFAULT 0,
  last_seq_processed bigint DEFAULT -1
);

CREATE INDEX IF NOT EXISTS idx_tracking_session_emp_status
  ON tracking_session (employee_id, status);

CREATE INDEX IF NOT EXISTS idx_tracking_session_geom
  ON tracking_session USING GIST (path_geometry);




ALTER TABLE tracking_session
  ADD COLUMN IF NOT EXISTS last_seq_processed bigint DEFAULT -1;
