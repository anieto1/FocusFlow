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
    private final int minAllowedParticipants = 1;

    //Work block config
    private final int minWorkDurationMinutes = 15;
    private final int maxWorkDurationMinutes = 180;

    //Short Break config
    private final int minShortBreakMinutes = 5;
    private final int maxShortBreakMinutes = 10;

    //Long break config
    private final int minLongBreakMinutes = 15;
    private final int maxLongBreakMinutes = 25;
}
