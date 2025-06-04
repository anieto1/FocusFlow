package com.pm.sessionservice.DTO;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UpdateSessionRequestDTO {

    @Size(max = 30, message= "Title cannot exceed 30 characters")
    private String sessionName;

    @Time
    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;


}
