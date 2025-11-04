-- One row per tracking session; keeps rollups and (optional) merged 2D path

CREATE TABLE IF NOT EXISTS tracking_session (
  id               BIGSERIAL PRIMARY KEY,
  employee_id      VARCHAR(64)        NOT NULL,
  status           VARCHAR(16)        NOT NULL,    -- 'OPEN' | 'CLOSED'
  started_at       TIMESTAMPTZ        NOT NULL,
  ended_at         TIMESTAMPTZ,
  start_lat        DOUBLE PRECISION,
  start_lng        DOUBLE PRECISION,
  last_lat         DOUBLE PRECISION,
  last_lng         DOUBLE PRECISION,
  last_seq         INTEGER            NOT NULL DEFAULT 0,
  total_points     INTEGER            NOT NULL DEFAULT 0,         -- rollup count
  total_distance_m DOUBLE PRECISION   NOT NULL DEFAULT 0,         -- meters (geodesic)
  path_geometry    geometry(LineString, 4326),                    -- merged 2D path at close
  created_at       TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_ts_status CHECK (status IN ('OPEN','CLOSED'))
);

CREATE INDEX IF NOT EXISTS idx_ts_emp_status   ON tracking_session(employee_id, status);
CREATE INDEX IF NOT EXISTS idx_ts_started_at   ON tracking_session(started_at);
CREATE INDEX IF NOT EXISTS idx_ts_path_geom_gx ON tracking_session USING GIST (path_geometry);

DROP TRIGGER IF EXISTS trg_ts_set_updated_at ON tracking_session;
CREATE TRIGGER trg_ts_set_updated_at
BEFORE UPDATE ON tracking_session
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
