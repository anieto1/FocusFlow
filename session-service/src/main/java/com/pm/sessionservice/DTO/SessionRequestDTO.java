package com.pm.sessionservice.DTO;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class SessionRequestDTO {

    @NotBlank(message = "ownerUsername is required")
    private String ownerUsername;

    @NotBlank
    @Size(max = 20, message = "Title cannot be more than 20 characters")
    private String sessionName;

    @Size(max = 500)
    private String description;

    private Integer maxParticipants;

    @Min(value = 15, message = "Work duration must be at least 15 minutes")
    @Max(value = 180, message = "Work duration cannot exceed 180 minutes")
    private Integer workDurationMinutes = 25;

    @Min(value = 5, message = "Short break must be at least 5 minutes")
    @Max(value = 10, message = "Short break cannot exceed 10 minutes")
    private Integer shortBreakMinutes = 5;

    @Min(value = 15, message = "Long break must be at least 15 minutes")
    @Max(value = 25, message = "Long break cannot exceed 25 minutes")
    private Integer longBreakMinutes = 15;

    @Size(max = 20, message = "Cannot assign more than 20 tasks to a session")
    private List<UUID> taskIds;

}