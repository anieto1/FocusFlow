package com.pm.sessionservice.DTO;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BreakSessionDTO {

    private UUID sessionId;
    private String sessionName;
    private Integer tasks;

}
