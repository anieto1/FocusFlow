package com.pm.sessionservice.DTO;

import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
public class SessionSummaryDTO {

    private UUID sessionId;
    private List<UUID> taskIDs;
    private List<UUID> participants;

}
