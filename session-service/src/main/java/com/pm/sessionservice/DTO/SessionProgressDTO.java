package com.pm.sessionservice.DTO;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SessionProgressDTO {

    private UUID sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration elapsedTime;
    private int taskCompleted;
    private List<UUID> activeParticipants;



}
