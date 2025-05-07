package com.pm.userservice.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequestDTO {

    @NotBlank(message = "First name required")
    @Size(max=30, message = "Name must be less than 30 characters")
    private String firstName;

    @NotBlank(message = "Last name required")
    @Size(max=30, message = "Name must be less than 30 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message="Invalid Email address")
    private String email;

    @Lob
    private byte[] profilePicture;

}
