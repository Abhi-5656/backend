CREATE TABLE IF NOT EXISTS tracking_path_chunk (
  chunk_id        BIGSERIAL PRIMARY KEY,
  session_id      bigint NOT NULL REFERENCES tracking_session(session_id) ON DELETE CASCADE,
  chunk_index     int    NOT NULL,
  point_count     int    NOT NULL,
  chunk_geometry  geometry(LineStringM, 4326) NOT NULL,
  started_at      timestamptz NOT NULL,
  ended_at        timestamptz NOT NULL,
  UNIQUE(session_id, chunk_index)
);

CREATE INDEX IF NOT EXISTS idx_tracking_chunk_session
  ON tracking_path_chunk (session_id, chunk_index);

CREATE INDEX IF NOT EXISTS idx_tracking_chunk_geom
  ON tracking_path_chunk USING GIST (chunk_geometry);
