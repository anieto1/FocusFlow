package com.pm.sessionservice.DTO;

import java.util.List;
import java.util.UUID;

public class SessionSummaryDTO {

    private UUID sessionId;
    private List<UUID>taskIDs;
    private List<UUID> participants;

}
