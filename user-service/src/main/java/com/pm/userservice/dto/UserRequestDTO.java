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

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }
    public byte[] getProfilePicture() {
        return profilePicture;
    }

}
