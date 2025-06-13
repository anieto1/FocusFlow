package com.pm.sessionservice.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UpdateSessionRequestDTO {

    @Size(max = 30, message= "Title cannot exceed 30 characters")
    @NotBlank
    private String sessionName;

    @NotBlank
    private LocalDateTime scheduledTime;

    @NotBlank
    private LocalDateTime startTime;

    @NotBlank
    private LocalDateTime endTime;


}
