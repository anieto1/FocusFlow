CREATE TABLE sessions (
                          session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          owner_username TEXT NOT NULL,
                          session_name TEXT NOT NULL,
                          scheduled_time TIMESTAMPTZ,
                          start_time TIMESTAMPTZ,
                          end_time TIMESTAMPTZ,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          status TEXT NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'ACTIVE', 'COMPLETED', 'CANCELLED',
                                                                                     'PAUSED')),
                          invite_code TEXT UNIQUE,
                          max_participants INTEGER DEFAULT 10,
                          description TEXT,

    -- Indexes for performance
                          CREATE INDEX idx_sessions_owner (owner_username),
                          CREATE INDEX idx_sessions_status (status),
                          CREATE INDEX idx_sessions_scheduled_time (scheduled_time),
                          CREATE INDEX idx_sessions_invite_code (invite_code)
);

CREATE TABLE session_participants (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      session_id UUID NOT NULL REFERENCES sessions(session_id) ON DELETE CASCADE,
                                      user_id UUID NOT NULL,
                                      joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      role TEXT NOT NULL DEFAULT 'PARTICIPANT' CHECK (role IN ('OWNER', 'PARTICIPANT')),
                                      is_active BOOLEAN DEFAULT true,

                                      UNIQUE(session_id, user_id),
                                      CREATE INDEX idx_session_participants_session (session_id),
                                      CREATE INDEX idx_session_participants_user (user_id)
);


CREATE TABLE session_audit_log (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   session_id UUID NOT NULL REFERENCES sessions(session_id),
                                   user_id UUID,
                                   action TEXT NOT NULL,
                                   old_values JSONB,
                                   new_values JSONB,
                                   timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                   CREATE INDEX idx_audit_session (session_id),
                                   CREATE INDEX idx_audit_timestamp (timestamp)
);
