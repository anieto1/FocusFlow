package com.pm.sessionservice.Service.Event;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SessionUpdatedEvent {
    private UUID sessionId;
    private String updatedBy;
    private String title;
}
