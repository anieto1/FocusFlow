package com.pm.sessionservice.DTO;

import com.pm.sessionservice.model.SessionStatus;
import com.pm.sessionservice.model.SessionType;
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
    private String inviteCode;
    private String description;
    private SessionType currentType;
    private Boolean isWaitingForBreakSelection;
    private List<UUID> taskIds;
    private Integer currentDurationMinutes;
    private LocalDateTime currentPhaseStartTime;
    private Integer totalWorkSessionsCompleted;
    private Integer maxParticipants;
    private Boolean isDeleted;
    private LocalDateTime updatedAt;
    private Integer workDurationMinutes;
    private Integer shortBreakMinutes;
    private Integer longBreakMinutes;
    private List<UUID> participantIds;

}
