package com.pm.sessionservice.Repository;

import com.pm.sessionservice.DTO.SessionResponseDTO;
import com.pm.sessionservice.model.Session;
import com.pm.sessionservice.model.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {


    boolean existsBySessionId(UUID sessionId);

    // Check if user has any active sessions (for conflict detection)
    boolean existsByOwnerUsernameAndStatus(String ownerUsername, SessionStatus status);

    @Query("SELECT s FROM Session s WHERE s.ownerUsername = :username " +
            "AND ((s.startTime BETWEEN :startTime AND :endTime) " +
            "OR (s.endTime BETWEEN :startTime AND :endTime)) " +
            "AND s.status IN :statuses")
    List<Session> findConflictingSessions(
            @Param("username") String username,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") SessionStatus... statuses);


    boolean existsByEmailAndSessionIdNot(UUID sessionId);


//find active sessions y user involved without pagination
    @Query("SELECT DISTINCT s FROM Session s " +
            "LEFT JOIN SessionParticipant sp ON s.sessionId = sp.sessionId " +
            "WHERE (s.ownerUsername = :username OR sp.userId = :userId) " +
            "AND s.status = 'ACTIVE' " +
            "AND s.isDeleted = false " +
            "ORDER BY s.startTime DESC " +
            "LIMIT 1")
    Optional<Session> findCurrentActiveSessionByUser(
            @Param("username") String username, 
            @Param("userId") UUID userId);



    @Query("SELECT s FROM Session s " +
            "WHERE s.inviteCode = :inviteCode " +
            "AND s.status = 'ACTIVE' " +
            "AND s.isDeleted = false")
    Optional<Session> findByInviteCode(@Param("inviteCode") String inviteCode);


}
