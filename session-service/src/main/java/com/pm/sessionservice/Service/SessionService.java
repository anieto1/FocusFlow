package com.pm.sessionservice.Service;

import com.pm.sessionservice.DTO.*;
import com.pm.sessionservice.model.SessionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SessionService {

    //CRUD operations
    SessionResponseDTO createSession(SessionRequestDTO sessionRequestDTO, UUID ownerId);
    SessionResponseDTO getSessionById(UUID sessionId, UUID userId);
    SessionResponseDTO updateSession(UUID sessionId, UpdateSessionRequestDTO request, UUID ownerId);
    void deleteSession(UUID sessionId, UUID ownerId);

    //Query Methods
    Page<SessionSummaryDTO> getSessionsByUser(UUID userId,Pageable pageable);
    Page<SessionSummaryDTO> getUpcomingSessions(UUID userId, Pageable pageable);
    Page<SessionSummaryDTO> getActiveSessionsByUser(UUID userId, Pageable pageable);
    List<SessionResponseDTO> getSessionsByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    //Session Lifecycle Management
    SessionResponseDTO startSession(UUID sessionId, UUID userId);
    SessionResponseDTO endSession(UUID sessionId, UUID userId, EndSessionRequestDTO endSessionRequestDTO);
    SessionResponseDTO resumeSession(UUID sessionId, UUID userId);
    SessionResponseDTO pauseSession(UUID sessionId, UUID userId);
    SessionResponseDTO extendSession(UUID sessionId, UUID userId, int addedTime);

    //Participant Management
    SessionResponseDTO inviteUser(UUID sessionId, UUID inviteeId, UUID inviterId);
    SessionResponseDTO removeUser(UUID sessionId, UUID userToRemove, UUID ownerId);
    SessionResponseDTO joinSession(UUID sessionId, UUID userId, String inviteCode);
    void leaveSession(UUID sessionId, UUID userId);
    List<UUID> getSessionParticipants(UUID sessionId, UUID requesterId);
    String generateInviteCode(UUID sessionId, UUID ownerId);

    //Permission and Access control
    boolean isUserSessionOwner(UUID sessionId, UUID userId);
    boolean canUserJoinSession(UUID sessionId, UUID userId, String inviteCode);

    // Validation & Business Rules
    void validateSessionTiming(LocalDateTime startTime, LocalDateTime endTime);
    void validateSessionCapacity(UUID sessionId, int additionalParticipants);
    boolean isSessionTimeSlotAvailable(UUID userId, LocalDateTime startTime, LocalDateTime endTime);

    //Pomodoro Phase Management
    SessionResponseDTO startWorkPhase(UUID sessionId, UUID userId);
    SessionResponseDTO startBreakPhase(UUID sessionId, UUID userId, SessionType breakType);
    SessionResponseDTO completeWorkPhase(UUID sessionId, UUID userId);
    SessionResponseDTO skipBreak(UUID sessionId, UUID userId);
    SessionProgressDTO getSessionProgress(UUID sessionId, UUID userId);
    BreakSessionDTO getBreakOptions(UUID sessionId, UUID userId);

    //Task Management within Sessions
    SessionResponseDTO addTaskToSession(UUID sessionId, UUID taskId, UUID userId);
    SessionResponseDTO removeTaskFromSession(UUID sessionId, UUID taskId, UUID userId);
    SessionResponseDTO markTaskCompleted(UUID sessionId, UUID taskId, UUID userId);
    List<UUID> getSessionTasks(UUID sessionId, UUID userId);

}
