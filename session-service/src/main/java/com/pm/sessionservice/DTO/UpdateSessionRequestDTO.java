package com.pm.sessionservice.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateSessionRequestDTO {

    @Size(max = 30, message= "Title cannot exceed 30 characters")
    @NotBlank
    private String sessionName;

    @NotNull
    private LocalDateTime scheduledTime;

    @Size(max=255, message = "Description cannot exceed 255 characters")
    private String sessionDescription;


}
