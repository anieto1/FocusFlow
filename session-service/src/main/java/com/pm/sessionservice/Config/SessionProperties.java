package com.pm.sessionservice.Config;

import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "session")
public class SessionProperties {
    private final int maxAllowedParticipants = 10;
    private final int minDurationMinutes = 15;
    private final int maxDurationMinutes = 180;
}
