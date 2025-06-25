package com.pm.sessionservice.DTO;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
public class SessionSummaryDTO {

    private UUID sessionId;
    private String sessionName;

    @Size(min = 1, max = 255)
    private String description;

    private List<UUID> taskIDs;
    private List<UUID> participants;
    private Integer totalWorkSessionsCompleted;


}
