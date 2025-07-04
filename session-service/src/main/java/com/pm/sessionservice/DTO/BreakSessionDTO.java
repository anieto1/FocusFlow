package com.pm.sessionservice.DTO;

import com.pm.sessionservice.model.SessionType;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class BreakSessionDTO {

    private UUID sessionId;
    private String sessionName;
    private Integer tasks;
    private SessionType currentType;        // WORK/SHORT_BREAK/LONG_BREAK
    private Integer workSessionsCompleted;  // For long break logic
    private Integer shortBreakMinutes;      // Duration options
    private Integer longBreakMinutes;       // Duration options
    private LocalDateTime phaseStartTime;   // When current phase began
    private Duration timeRemaining;

}
