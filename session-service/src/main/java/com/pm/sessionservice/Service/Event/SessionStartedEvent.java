package com.pm.sessionservice.Service.Event;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;
import java.time.LocalDateTime;

@Data
@Builder
public class SessionStartedEvent {
    private UUID sessionId;
    private LocalDateTime startTime;
}