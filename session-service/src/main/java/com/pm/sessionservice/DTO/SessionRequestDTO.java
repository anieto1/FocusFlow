package com.pm.sessionservice.DTO;

import com.pm.sessionservice.model.SessionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SessionRequestDTO {

    @NotBlank(message = "ownerUsername is required")
    private String ownerUsername;

    @NotBlank
    @Size(max = 20, message = "Title cannot be more than 20 characters")
    private String sessionName;

    @Size(max = 10, message = "Cannot invite more than 10 users")
    private List<UUID> userIds;
    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SessionStatus status;
    // Getters and Setters

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getSessionName() { return sessionName; }

    public void setSessionName(String sessionName) { this.sessionName = sessionName; }

    public List<UUID> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<UUID> userIds) {
        this.userIds = userIds;
    }

    public LocalDateTime getStartTime(){ return startTime; }

    public void setStartTime(LocalDateTime startTime){ this.startTime = startTime; }

    public LocalDateTime getEndTime(){ return endTime; }

    public void setEndTime(LocalDateTime endTime){ this.endTime = endTime; }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public SessionStatus getStatus() { return status; }

    public void setStatus(SessionStatus status) { this.status = status; }
}