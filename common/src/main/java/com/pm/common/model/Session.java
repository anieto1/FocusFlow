package com.pm.common.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID sessionId;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @ManyToMany
    @JoinTable(name = "session_users", joinColumns = @JoinColumn(name = "session_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> members = new ArrayList<User>();


}
