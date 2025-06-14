package com.pm.sessionservice.DTO;

import com.pm.sessionservice.model.SessionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SessionResponseDTO {

    private UUID sessionId;
    private String ownerUsername;
    private String sessionName;
    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private List<UUID> userIds;
    private SessionStatus status;

}
