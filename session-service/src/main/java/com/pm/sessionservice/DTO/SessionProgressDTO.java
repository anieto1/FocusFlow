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
    private Duration elapsedTime;
    private Integer taskCompleted;
    private List<UUID> activeParticipants;



}
