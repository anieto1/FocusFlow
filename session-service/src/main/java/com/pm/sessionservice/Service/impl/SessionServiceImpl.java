package com.pm.sessionservice.Service.impl;


import com.pm.sessionservice.Config.SessionProperties;
import com.pm.sessionservice.DTO.*;
import com.pm.sessionservice.Exception.InvalidSessionDataException;
import com.pm.sessionservice.Exception.SessionAccessDeniedException;
import com.pm.sessionservice.Exception.SessionException;
import com.pm.sessionservice.Mapper.SessionMapper;
import com.pm.sessionservice.Repository.SessionRepository;
import com.pm.sessionservice.Service.SessionService;
import com.pm.sessionservice.model.Session;
import com.pm.sessionservice.model.SessionType;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@RequiredArgsConstructor
@Service
public class SessionServiceImpl implements SessionService {
    private static final Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);
    private final SessionRepository sessionRepository;
    private final SessionProperties sessionProperties;
    private final SessionMapper sessionMapper;

    //CRUD operations
    @Transactional(readOnly = true)
    public SessionResponseDTO createSession(SessionRequestDTO sessionRequestDTO, UUID ownerId){

        if(hasActiveSession(ownerId)){
            throw new SessionAccessDeniedException("Cannot start a new session with a active session ");
        }

        Session newSession = sessionRepository.save(sessionMapper.fromRequestDTO(sessionRequestDTO));



        return sessionMapper.toResponseDTO(newSession);
    }
    SessionResponseDTO getSessionById(UUID sessionId, UUID userId);
    SessionResponseDTO updateSession(UUID sessionId, UpdateSessionRequestDTO request, UUID ownerId);
    void deleteSession(UUID sessionId, UUID ownerId);

    //Query Methods
    Page<SessionSummaryDTO> getSessionsByUser(UUID userId,Pageable pageable);
    Page<SessionSummaryDTO> getActiveSessionsByUser(UUID userId, Pageable pageable);
    List<SessionResponseDTO> getSessionsByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);




    // New utility methods

    //Placeholder for GRPC
    private String getUsernameFromUserId(UUID userId) {
        // TODO: Replace with actual call to user-service
        // Example: return userServiceClient.getUserById(userId).getUsername();

        // For now, return a placeholder
        log.warn("Using placeholder username lookup for userId: {}", userId);
        return "user_" + userId.toString().substring(0, 8);
    }

    public SessionResponseDTO getCurrentActiveSession(UUID userId){
        String username = getUsernameFromUserId(userId);
        Optional<Session> activeSession = sessionRepository.findCurrentActiveSessionByUser(username, userId);

        return activeSession
                .map(sessionMapper :: toResponseDTO)
                .orElseThrow(()-> new SessionException("No active session found for user: " + username));
    }
    public boolean hasActiveSession(UUID userId){
        return getCurrentActiveSession(userId) != null;
    }

    public SessionResponseDTO getSessionByInviteCode(String inviteCode){
        if(inviteCode == null || inviteCode.isEmpty()){
            throw new InvalidSessionDataException("there is no invite code");
        }
        Optional<Session> inviteSession = sessionRepository.findByInviteCode(inviteCode);
        if(inviteSession.isEmpty()){
            throw new InvalidSessionDataException("there is no invite code");
        }

        return inviteSession
                .map(sessionMapper ::toResponseDTO)
                .orElseThrow(()-> new SessionException("No active session found with invite code: " + inviteCode));
    }

    //Session Lifecycle Management
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
    void validateSessionCapacity(UUID sessionId, int additionalParticipants);

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
