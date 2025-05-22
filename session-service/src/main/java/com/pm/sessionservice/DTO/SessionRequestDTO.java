package com.pm.sessionservice.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SessionRequestDTO {
    @NotBlank(message = "OwnerId is required")
    @Size(max = 30, message = "Owner Username must be less than 30 characters")
    private String ownerUserName;

    @Size(max = 10, message ="No more than 10 users")
    private String[] participants;
    private String title;
    private String start_time;
    private String end_time;
    private String[] messages;
    private boolean is_active;
    private String created_at;
}
