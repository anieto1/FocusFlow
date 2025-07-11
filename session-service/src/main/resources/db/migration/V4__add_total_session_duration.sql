-- Add total session duration field to sessions table
ALTER TABLE sessions ADD COLUMN total_session_duration_minutes BIGINT;

-- Add index for analytics queries on session duration
CREATE INDEX idx_sessions_total_duration ON sessions(total_session_duration_minutes);