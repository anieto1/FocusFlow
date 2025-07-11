-- Add current participant count field to sessions table
ALTER TABLE sessions ADD COLUMN current_participant_count INTEGER NOT NULL DEFAULT 1;

-- Add index for participant queries
CREATE INDEX idx_sessions_participant_count ON sessions(current_participant_count);

-- Add check constraint to ensure current count doesn't exceed max
ALTER TABLE sessions ADD CONSTRAINT chk_participant_count_within_max 
    CHECK (current_participant_count <= max_participants);