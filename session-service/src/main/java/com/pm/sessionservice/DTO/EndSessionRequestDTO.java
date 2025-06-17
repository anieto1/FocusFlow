package com.pm.sessionservice.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class EndSessionRequestDTO {
    private UUID sessionIDs;
    private UUID ownerID;
    private LocalDateTime endTime;
    private String summaryNote;
    private List<UUID> completedTaskIDs;
    private List<UUID> incompleteTaskIDs;
    private List<UUID> participantIDs;


  }