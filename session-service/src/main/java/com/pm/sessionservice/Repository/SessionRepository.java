package com.pm.sessionservice.Repository;

import com.pm.sessionservice.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    boolean existsBySessionId(UUID sessionId);
}
