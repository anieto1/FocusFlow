-- Add user's base pomodoro configuration fields to sessions table
ALTER TABLE sessions ADD COLUMN work_duration_minutes INTEGER NOT NULL DEFAULT 25;
ALTER TABLE sessions ADD COLUMN short_break_duration_minutes INTEGER NOT NULL DEFAULT 5;
ALTER TABLE sessions ADD COLUMN long_break_duration_minutes INTEGER NOT NULL DEFAULT 15;

-- Add constraints to ensure valid duration ranges
ALTER TABLE sessions ADD CONSTRAINT work_duration_range CHECK (work_duration_minutes >= 15 AND work_duration_minutes <= 180);
ALTER TABLE sessions ADD CONSTRAINT short_break_duration_range CHECK (short_break_duration_minutes >= 5 AND short_break_duration_minutes <= 10);
ALTER TABLE sessions ADD CONSTRAINT long_break_duration_range CHECK (long_break_duration_minutes >= 15 AND long_break_duration_minutes <= 25);

-- Create indexes for potential queries on duration fields
CREATE INDEX idx_sessions_work_duration ON sessions(work_duration_minutes);
CREATE INDEX idx_sessions_short_break_duration ON sessions(short_break_duration_minutes);
CREATE INDEX idx_sessions_long_break_duration ON sessions(long_break_duration_minutes);