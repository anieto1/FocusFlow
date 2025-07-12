package com.pm.sessionservice.Repository;

import com.pm.sessionservice.model.SessionParticipant;
import com.pm.sessionservice.model.ParticipantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, UUID> {

    // Core participant queries
    @Query("SELECT sp FROM SessionParticipant sp WHERE sp.sessionId = :sessionId AND sp.isActive = true")
    List<SessionParticipant> findActiveParticipantsBySessionId(@Param("sessionId") UUID sessionId);

    @Query("SELECT sp FROM SessionParticipant sp WHERE sp.sessionId = :sessionId AND sp.userId = :userId AND sp.isActive = true")
    Optional<SessionParticipant> findActiveParticipant(@Param("sessionId") UUID sessionId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(sp) FROM SessionParticipant sp WHERE sp.sessionId = :sessionId AND sp.isActive = true")
    int countActiveParticipantsBySessionId(@Param("sessionId") UUID sessionId);

    // Participant validation
    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END FROM SessionParticipant sp " +
           "WHERE sp.sessionId = :sessionId AND sp.userId = :userId AND sp.isActive = true")
    boolean isUserActiveParticipant(@Param("sessionId") UUID sessionId, @Param("userId") UUID userId);

    // Get participant user IDs (for integration with user service)
    @Query("SELECT sp.userId FROM SessionParticipant sp WHERE sp.sessionId = :sessionId AND sp.isActive = true")
    List<UUID> findActiveParticipantUserIds(@Param("sessionId") UUID sessionId);

    // Participant role queries
    @Query("SELECT sp FROM SessionParticipant sp WHERE sp.sessionId = :sessionId AND sp.role = :role AND sp.isActive = true")
    List<SessionParticipant> findParticipantsByRole(@Param("sessionId") UUID sessionId, @Param("role") ParticipantRole role);

    // Soft delete participant (remove from session)
    @Modifying
    @Query("UPDATE SessionParticipant sp SET sp.isActive = false, sp.lastLeftTime = :leftTime " +
           "WHERE sp.sessionId = :sessionId AND sp.userId = :userId AND sp.isActive = true")
    int removeParticipantFromSession(@Param("sessionId") UUID sessionId, 
                                   @Param("userId") UUID userId, 
                                   @Param("leftTime") LocalDateTime leftTime);

    // Update participant session status
    @Modifying
    @Query("UPDATE SessionParticipant sp SET sp.isCurrentlyInSession = :inSession " +
           "WHERE sp.sessionId = :sessionId AND sp.userId = :userId AND sp.isActive = true")
    int updateParticipantSessionStatus(@Param("sessionId") UUID sessionId, 
                                     @Param("userId") UUID userId, 
                                     @Param("inSession") boolean inSession);

    // Time tracking updates
    @Modifying
    @Query("UPDATE SessionParticipant sp SET sp.currentSessionStartTime = :startTime, sp.isCurrentlyInSession = true " +
           "WHERE sp.sessionId = :sessionId AND sp.userId = :userId AND sp.isActive = true")
    int updateParticipantJoinTime(@Param("sessionId") UUID sessionId, 
                                @Param("userId") UUID userId, 
                                @Param("startTime") LocalDateTime startTime);

    // Cleanup methods for session lifecycle
    @Modifying
    @Query("UPDATE SessionParticipant sp SET sp.isActive = false, sp.lastLeftTime = :endTime " +
           "WHERE sp.sessionId = :sessionId AND sp.isActive = true")
    int deactivateAllParticipants(@Param("sessionId") UUID sessionId, @Param("endTime") LocalDateTime endTime);

    // User session history (for integration with user service)
    @Query("SELECT sp.sessionId FROM SessionParticipant sp WHERE sp.userId = :userId")
    List<UUID> findSessionIdsByUserId(@Param("userId") UUID userId);
}