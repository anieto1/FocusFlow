package com.pm.sessionservice.DTO;

import com.pm.sessionservice.model.SessionStatus;
import com.pm.sessionservice.model.SessionType;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SessionProgressDTO {

    private UUID sessionId;
    private String sessionName;
    private SessionStatus status;
    private SessionType currentType;
    private LocalDateTime startTime;
    private LocalDateTime currentPhaseStartTime;
    private Duration elapsedTime;
    private Duration timeRemainingInPhase;
    private Integer currentDurationMinutes;
    private Integer tasksCompleted;
    private Integer totalTasks;
    private Integer totalWorkSessionsCompleted;
    private List<UUID> activeParticipants;
    private List<UUID> completedTaskIds;
    private Boolean isWaitingForBreakSelection;

}
