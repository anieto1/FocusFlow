package com.pm.sessionservice.DTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SessionProgressDTO {

    private UUID sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration elapsedTime;
    private int taskCompleted;
    private List<String> activeParticipants;
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Duration getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Duration elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getTaskCompleted() {
        return taskCompleted;
    }

    public void setTaskCompleted(int taskCompleted) {
        this.taskCompleted = taskCompleted;
    }

    public List<String> getActiveParticipants() {
        return activeParticipants;
    }

    public void setActiveParticipants(List<String> activeParticipants) {
        this.activeParticipants = activeParticipants;
    }



}
