package com.pm.chatservice.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class Messages {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID message_id;

    @Column
    private UUID session_id;

    @Column
    @NotNull
    private UUID sender_id;

    @Column
    @NotNull
    private String content;

    @Column
    @NotNull
    private LocalDateTime timestamp;
}
