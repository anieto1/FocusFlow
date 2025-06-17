package com.pm.sessionservice.model;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "session_participants")
@Data
public class SessionParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Store just the UUID - no JPA relationship
    @NotNull
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;  // References user-service, not a JPA entity

    @CreatedDate
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ParticipantRole role = ParticipantRole.PARTICIPANT;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Time tracking for individual participants
    @Column(name = "current_session_start_time")
    private LocalDateTime currentSessionStartTime;  // When they last joined/rejoined

    @Column(name = "total_session_time_minutes")
    private Integer totalSessionTimeMinutes = 0;  // Cumulative time spent in session

    @Column(name = "last_left_time")
    private LocalDateTime lastLeftTime;  // When they last left (for calculating time)

    // Pomodoro participation tracking
    @Column(name = "work_sessions_participated")
    private Integer workSessionsParticipated = 0;

    @Column(name = "is_currently_in_session")
    private Boolean isCurrentlyInSession = false;  // Currently online/active in session
}
