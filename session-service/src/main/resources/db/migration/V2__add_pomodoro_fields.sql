-- Add missing columns to sessions table for pomodoro functionality
ALTER TABLE sessions ADD COLUMN is_deleted BOOLEAN DEFAULT false;
ALTER TABLE sessions ADD COLUMN current_type TEXT NOT NULL DEFAULT 'WORK' CHECK (current_type IN ('WORK', 'SHORT_BREAK', 'LONG_BREAK'));
ALTER TABLE sessions ADD COLUMN current_duration_minutes INTEGER NOT NULL DEFAULT 25;
ALTER TABLE sessions ADD COLUMN current_phase_start_time TIMESTAMPTZ;
ALTER TABLE sessions ADD COLUMN total_work_sessions_completed INTEGER DEFAULT 0;
ALTER TABLE sessions ADD COLUMN is_waiting_for_break_selection BOOLEAN DEFAULT false;

-- Create session_tasks table for task references
CREATE TABLE session_tasks (
    session_id UUID NOT NULL REFERENCES sessions(session_id) ON DELETE CASCADE,
    task_id UUID NOT NULL,
    PRIMARY KEY (session_id, task_id),
    CREATE INDEX idx_session_tasks_session (session_id),
    CREATE INDEX idx_session_tasks_task (task_id)
);

-- Update the status check constraint to include PAUSED
ALTER TABLE sessions DROP CONSTRAINT IF EXISTS sessions_status_check;
ALTER TABLE sessions ADD CONSTRAINT sessions_status_check 
    CHECK (status IN ('SCHEDULED', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'PAUSED'));