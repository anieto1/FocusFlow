package com.pm.sessionservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessions", indexes = {
        @Index(name = "idx_sessions_owner", columnList = "owner_username"),
        @Index(name = "idx_sessions_status", columnList = "status"),
        @Index(name = "idx_sessions_scheduled_time", columnList = "scheduled_time")
})

@Data
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id", updatable = false, nullable = false)
    private UUID sessionId;

    @NotBlank
    @Column(name = "owner_username", nullable = false, length = 50)
    private String ownerUsername;

    @NotBlank
    @Size(min = 1, max = 100)
    @Column(name = "session_name", nullable = false)
    private String sessionName;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(name = "invite_code", unique = true, length = 8)
    private String inviteCode;

    @Min(1) @Max(50)
    @Column(name = "max_participants")
    private Integer maxParticipants = 10;

    @Size(max = 500)
    @Column(name = "description")
    private String description;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // Pomodoro-specific fields
    @Enumerated(EnumType.STRING)
    @Column(name = "current_type", nullable = false)
    private SessionType currentType = SessionType.WORK;

    @Column(name = "current_duration_minutes", nullable = false)
    private Integer currentDurationMinutes = 25;

    @Column(name = "current_phase_start_time")
    private LocalDateTime currentPhaseStartTime;

    @Column(name = "total_work_sessions_completed")
    private Integer totalWorkSessionsCompleted = 0;

    @Column(name = "is_waiting_for_break_selection")
    private Boolean isWaitingForBreakSelection = false;

    // Task references (task-service manages actual tasks)
    @ElementCollection
    @CollectionTable(name = "session_tasks", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "task_id")
    private List<UUID> taskIds = new ArrayList<>();
}
