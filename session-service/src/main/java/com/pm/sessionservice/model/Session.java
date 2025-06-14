package com.pm.sessionservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessions")
@Data
public class Session {

    @Id
    @GeneratedValue
    @Column(name = "session_id", updatable = false, nullable = false)
    private UUID sessionId;

    @NotNull
    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    @NotNull
    @Column(name = "session_name", nullable = false)
    private String sessionName;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @ElementCollection
    @CollectionTable(
            name = "session_users",
            joinColumns = @JoinColumn(name = "session_id")
    )
    @Column(name = "user_id", nullable = false)
    private List<UUID> userIds = new ArrayList<>();

    @Column
    private String inviteCode;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
