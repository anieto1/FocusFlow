package com.pm.sessionservice.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SessionRequestDTO {

    @NotBlank(message = "ownerUsername is required")
    private String ownerUsername;

    @Size(max = 10, message = "Cannot invite more than 10 users")
    private List<UUID> userIds;
    private String title;
    private int timeHr;
    private int timeMin;
    private LocalDateTime scheduledTime;

    // Getters and Setters

    public String getTitle() { return title;}

    public void setTitle(String title) { this.title = title;}

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public List<UUID> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<UUID> userIds) {
        this.userIds = userIds;
    }

    public int getTimeHr() { return timeHr;}

    public void setTimeHr(int timeHr) { this.timeHr = timeHr;}

    public int getTimeMin() { return timeMin;}

    public void setTimeMin(int timeMin) { this.timeMin = timeMin;}

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}