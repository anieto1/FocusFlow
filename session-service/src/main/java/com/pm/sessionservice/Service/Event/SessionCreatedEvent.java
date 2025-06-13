package com.pm.sessionservice.Service.Event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionCreatedEvent {
    private UUID sessionId;
    private String creatorUserId;
    private String title;
    private LocalDateTime startTime;
}
