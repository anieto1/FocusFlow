package com.pm.sessionservice.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UpdateSessionRequestDTO {

    @Size(max = 30, message= "Title cannot exceed 30 characters")
    @NotBlank
    private String sessionName;

    @NotNull
    private LocalDateTime scheduledTime;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;


}
