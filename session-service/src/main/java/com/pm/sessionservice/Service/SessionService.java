package com.pm.sessionservice.Service;

import com.pm.sessionservice.DTO.*;
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
    SessionResponseDTO inviteUser(UUID sessionId, UUID userId);
    SessionResponseDTO removeUser(UUID sessionId, UUID userId);
    SessionResponseDTO joinSession(UUID sessionId, UUID userId, String inviteCode);
    void leaveSession(UUID sessionId, UUID userId);
    List<SessionResponseDTO> getSessionUsers(UUID sessionId, UUID userId);

    //Permission and Access control
    boolean isUserSessionOwner(UUID sessionId, String userId);
    boolean canUserJoinSession(UUID sessionId, UUID userId, String inviteCode);

    // Validation & Business Rules
    void validateSessionTiming(LocalDateTime startTime, LocalDateTime endTime);
    void validateSessionCapacity(UUID sessionId, int additionalParticipants);
    boolean isSessionTimeSlotAvailable(String userId, LocalDateTime startTime, LocalDateTime endTime);
















    //    List<SessionResponseDTO> getSessions();
//    SessionResponseDTO getSession(UUID sessionId);
//
//    SessionResponseDTO startSession(UUID sessionId);
//    SessionResponseDTO stopSession(UUID sessionId);
//
//    SessionResponseDTO addUserToSession(UUID sessionId, String userName);
//    List<String> getUsersInSession(UUID sessionId);
//
//    List<SessionResponseDTO> getSessionsByUser(UUID userId);
//    List<SessionResponseDTO> getUpcomingSessions();

}
