package com.pm.userservice.dto;

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

    @NotBlank(message = "Password is required")
    @Size(min=8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Username is required")
    @Size(max = 30, message = "Username must be under 30 characters")
    private String username;

    @Size(max=2000000, message = "Profile picture must be less than 2MB")
    private String profilePictureUrl;

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
    public String getUsername(){return username;}
    public void setUsername(String username){this.username=username;}
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

}
