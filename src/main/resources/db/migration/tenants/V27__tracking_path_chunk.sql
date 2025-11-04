-- Chunks store compressed batches of points as LineStringM (lon,lat,M=epochSeconds)

CREATE TABLE IF NOT EXISTS tracking_path_chunk (
  chunk_id        BIGSERIAL PRIMARY KEY,
  session_id      BIGINT NOT NULL REFERENCES tracking_session(id) ON DELETE CASCADE,
  chunk_index     INT    NOT NULL,                            -- 0..N ordering within a session
  point_count     INT    NOT NULL,
  chunk_geometry  geometry(LineStringM, 4326) NOT NULL,
  started_at      TIMESTAMPTZ NOT NULL,                       -- min timestamp in chunk
  ended_at        TIMESTAMPTZ NOT NULL,                       -- max timestamp in chunk
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(session_id, chunk_index)
);

CREATE INDEX IF NOT EXISTS idx_tpch_session_order  ON tracking_path_chunk (session_id, chunk_index);
CREATE INDEX IF NOT EXISTS idx_tpch_geom_gx        ON tracking_path_chunk USING GIST (chunk_geometry);
CREATE INDEX IF NOT EXISTS idx_tpch_started_at     ON tracking_path_chunk (started_at);

DROP TRIGGER IF EXISTS trg_tpch_set_updated_at ON tracking_path_chunk;
CREATE TRIGGER trg_tpch_set_updated_at
BEFORE UPDATE ON tracking_path_chunk
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- Convenience view: merged path as GeoJSON from chunks (works for OPEN & CLOSED)
CREATE OR REPLACE VIEW v_session_merged_path AS
SELECT
  s.id AS session_id,
  ST_AsGeoJSON(
    ST_LineMerge(
      ST_Collect(c.chunk_geometry ORDER BY c.chunk_index)
    )
  ) AS geojson
FROM tracking_session s
LEFT JOIN tracking_path_chunk c ON c.session_id = s.id
GROUP BY s.id;

