package com.pm.sessionservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID sessionId;
    
    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String ownerUserName;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<UUID> participants = new ArrayList<>();
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private LocalDateTime start_time;
    
    @Column(nullable = false)
    private LocalDateTime end_time;

    @Column(nullable = false)
    private List<StringBuilder> messages = new ArrayList<>();
    
    @Column
    private boolean is_active;
    
    @Column(nullable = false)
    private LocalDateTime created_at;


    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUserName() {return ownerUserName;}

    public void setOwnerUserName(String ownerUserName) {this.ownerUserName = ownerUserName;}

    public List<UUID> getParticipants() {return participants;}

    public void setParticipants(List<UUID> participants) {this.participants = participants;}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStart_time() {
        return start_time;
    }

    public void setStart_time(LocalDateTime start_time) {
        this.start_time = start_time;
    }

    public LocalDateTime getEnd_time() {
        return end_time;
    }

    public void setEnd_time(LocalDateTime end_time) {
        this.end_time = end_time;
    }

    public List<StringBuilder> getMessages() {return messages;}

    public void setMessages(List<StringBuilder> messages) {this.messages = messages;}

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
