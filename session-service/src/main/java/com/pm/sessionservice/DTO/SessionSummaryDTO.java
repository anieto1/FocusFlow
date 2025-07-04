package com.pm.sessionservice.DTO;

import com.pm.sessionservice.model.SessionStatus;
import com.pm.sessionservice.model.SessionType;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Data
public class SessionSummaryDTO {

    private UUID sessionId;
    private String sessionName;
    private String ownerUsername;
    private SessionStatus status;
    private SessionType currentType;
    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
    private LocalDateTime createdAt;

    @Size(min = 1, max = 255)
    private String description;

    private List<UUID> taskIds;
    private List<UUID> participants;
    private Integer totalWorkSessionsCompleted;
    private Integer maxParticipants;
    private Boolean isWaitingForBreakSelection;

}
