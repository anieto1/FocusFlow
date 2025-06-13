package com.pm.sessionservice.DTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EndSessionRequestDTO {
    private UUID sessionIDs;
    private UUID ownerID;
    private LocalDateTime endTime;
    private String summaryNote;
    private List<UUID> completedTaskIDs;
    private List<UUID>incompleteTaskIDs;
    private List<UUID> participantIDs;


    public UUID getSessionIDs() {
        return sessionIDs;
    }

    public void setSessionIDs(UUID sessionIDs) { this.sessionIDs = sessionIDs; }

    public UUID getOwnerID() {
        return ownerID;
    }
    public void setOwnerID(UUID ownerID) { this.ownerID = ownerID; }

    public LocalDateTime getEndTime() { return endTime; }

    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getSummaryNote() { return summaryNote; }

    public void setSummaryNote(String summaryNote) { this.summaryNote = summaryNote; }

    public List<UUID> getCompletedTaskIDs() { return completedTaskIDs; }

    public void setCompletedTaskIDs(List<UUID> completedTaskIDs) {this.completedTaskIDs = completedTaskIDs; }

    public List<UUID> getIncompleteTaskIDs() { return incompleteTaskIDs; }

    public void setIncompleteTaskIDs(List<UUID> incompleteTaskIDs) {this.incompleteTaskIDs = incompleteTaskIDs;}

    public List<UUID> getParticipantIDs() { return participantIDs; }

    public void setParticipantIDs(List<UUID> participantIDs) {this.participantIDs = participantIDs;}
}