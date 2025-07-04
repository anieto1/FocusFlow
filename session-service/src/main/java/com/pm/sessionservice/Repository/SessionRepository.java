package com.pm.sessionservice.Repository;

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
import java.util.UUID;
@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    Page<Session> findByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername, Pageable pageable);

    Page<Session> findByUserIdsContainingOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Session> findByOwnerUsernameAndStatusAndScheduledTimeAfter(
            String ownerUsername, SessionStatus status, LocalDateTime scheduledTime);

    List<Session> findByUserIdsContainingAndStatusAndScheduledTimeAfter(
            UUID userId, SessionStatus status, LocalDateTime scheduledTime);

    List<Session> findByOwnerUsernameAndStatus(String ownerUsername, SessionStatus status);

    List<Session> findByUserIdsContainingAndStatus(UUID userId, SessionStatus status);

    boolean existsBySessionId(UUID sessionId);


    @Query("SELECT s FROM Session s WHERE s.ownerUsername = :username " +
            "AND ((s.scheduledTime BETWEEN :startTime AND :endTime) " +
            "OR (s.startTime BETWEEN :startTime AND :endTime) " +
            "OR (s.endTime BETWEEN :startTime AND :endTime)) " +
            "AND s.status IN :statuses")
    List<Session> findConflictingSessions(
            @Param("username") String username,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") SessionStatus... statuses);


    boolean existsByEmailAndSessionIdNot(UUID sessionId);

    // Find sessions where user is either owner or participant
    @Query("SELECT DISTINCT s FROM Session s " +
           "LEFT JOIN SessionParticipant sp ON s.sessionId = sp.sessionId " +
           "WHERE s.ownerUsername = :username OR sp.userId = :userId " +
           "ORDER BY s.createdAt DESC")
    Page<Session> findSessionsByUserInvolved(
            @Param("username") String username,
            @Param("userId") UUID userId,
            Pageable pageable);

    // Find upcoming sessions where user is owner or participant
    @Query("SELECT DISTINCT s FROM Session s " +
           "LEFT JOIN SessionParticipant sp ON s.sessionId = sp.sessionId " +
           "WHERE (s.ownerUsername = :username OR sp.userId = :userId) " +
           "AND s.scheduledTime > :currentTime " +
           "AND s.status = 'SCHEDULED' " +
           "AND s.isDeleted = false " +
           "ORDER BY s.scheduledTime ASC")
    Page<Session> findUpcomingSessionsByUserInvolved(
            @Param("username") String username,
            @Param("userId") UUID userId,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);


    // Find active sessions where user is owner or participant
    @Query("SELECT DISTINCT s FROM Session s " +
           "LEFT JOIN SessionParticipant sp ON s.sessionId = sp.sessionId " +
           "WHERE (s.ownerUsername = :username OR sp.userId = :userId) " +
           "AND s.status = 'ACTIVE' " +
           "AND s.isDeleted = false " +
           "ORDER BY s.startTime DESC")
    Page<Session> findActiveSessionsByUserInvolved(
            @Param("username") String username,
            @Param("userId") UUID userId,
            Pageable pageable);

    // Find sessions by date range where user is owner or participant
    @Query("SELECT DISTINCT s FROM Session s " +
           "LEFT JOIN SessionParticipant sp ON s.sessionId = sp.sessionId " +
           "WHERE (s.ownerUsername = :username OR sp.userId = :userId) " +
           "AND s.isDeleted = false " +
           "AND (s.scheduledTime BETWEEN :startDate AND :endDate " +
           "     OR s.startTime BETWEEN :startDate AND :endDate " +
           "     OR s.endTime BETWEEN :startDate AND :endDate) " +
           "ORDER BY s.scheduledTime DESC")
    List<Session> findSessionsByDateRangeInvolved(
            @Param("username") String username,
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

}
