package com.pm.sessionservice.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateSessionRequestDTO {

    @Size(max = 20, message= "Title cannot exceed 20 characters")
    @NotBlank
    private String sessionName;

    @Size(max=255, message = "Description cannot exceed 255 characters")
    private String description;

    private Integer maxParticipants;

    @Min(value = 5, message = "Work duration must be at least 5 minutes")
    @Max(value = 60, message = "Work duration cannot exceed 60 minutes")
    private Integer workDurationMinutes;

    @Min(value = 1, message = "Short break must be at least 1 minute")
    @Max(value = 30, message = "Short break cannot exceed 30 minutes")
    private Integer shortBreakMinutes;

    @Min(value = 5, message = "Long break must be at least 5 minutes")
    @Max(value = 60, message = "Long break cannot exceed 60 minutes")
    private Integer longBreakMinutes;

}
