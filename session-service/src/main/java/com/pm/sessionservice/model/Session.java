package com.pm.sessionservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID sessionId;

    @Column(name = "admin_id",nullable = false)
    private UUID adminId;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @ElementCollection
    @CollectionTable(name = "session_users", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "user_id")
    private List<UUID> memberIds = new ArrayList<>();

    @Column(name = "scheduled_at",nullable = false )
    private LocalDateTime scheduledAt;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;





}
