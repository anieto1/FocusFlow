package com.pm.sessionservice.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class EndSessionRequestDTO {
    @NotNull(message = "Session ID is required")
    private UUID sessionId;
    
    @NotNull(message = "Owner ID is required")
    private UUID ownerID;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @Size(max = 500, message = "Summary note cannot exceed 500 characters")
    private String summaryNote;
    
    private List<UUID> completedTaskIDs;
    private List<UUID> incompleteTaskIDs;
    private List<UUID> participantIDs;
    private Integer totalWorkSessionsCompleted;
    private Integer totalTasksCompleted;

}