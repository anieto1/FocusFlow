package com.pm.sessionservice.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionRequestDTO {

    @NotBlank(message = "ownerUsername is required")
    private String ownerUsername;

    @NotBlank
    @Size(max = 20, message = "Title cannot be more than 20 characters")
    private String sessionName;

    private LocalDateTime scheduledTime;

    @Size(max = 500)
    private String description;
}