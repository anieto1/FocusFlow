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

    @Min(value = 15, message = "Work duration must be at least 15 minutes")
    @Max(value = 180, message = "Work duration cannot exceed 180 minutes")
    private Integer workDurationMinutes;

    @Min(value = 5, message = "Short break must be at least 5 minutes")
    @Max(value = 10, message = "Short break cannot exceed 10 minutes")
    private Integer shortBreakMinutes;

    @Min(value = 15, message = "Long break must be at least 15 minutes")
    @Max(value = 25, message = "Long break cannot exceed 25 minutes")
    private Integer longBreakMinutes;

}
