package com.pm.sessionservice.DTO;

import com.pm.sessionservice.model.SessionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SessionRequestDTO {

    private UUID sessionId;

    @NotBlank(message = "ownerUsername is required")
    private String ownerUsername;

    @NotBlank
    @Size(max = 20, message = "Title cannot be more than 20 characters")
    private String sessionName;

    @Size(max = 10, message = "Cannot invite more than 10 users")
    private List<UUID> userIds;
    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SessionStatus status;
}