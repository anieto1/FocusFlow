package com.pm.sessionservice.Service.impl;


import com.pm.sessionservice.Config.SessionProperties;
import com.pm.sessionservice.DTO.*;
import com.pm.sessionservice.Exception.InvalidSessionDataException;
import com.pm.sessionservice.Exception.SessionAccessDeniedException;
import com.pm.sessionservice.Exception.SessionException;
import com.pm.sessionservice.Exception.SessionNotFoundException;
import com.pm.sessionservice.Mapper.SessionMapper;
import com.pm.sessionservice.Repository.SessionRepository;
import com.pm.sessionservice.Service.SessionService;
import com.pm.sessionservice.model.Session;
import com.pm.sessionservice.model.SessionStatus;
import com.pm.sessionservice.model.SessionType;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;


@RequiredArgsConstructor
@Service
public class SessionServiceImpl implements SessionService {
    private static final Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);
    private final SessionRepository sessionRepository;
    private final SessionProperties sessionProperties;
    private final SessionMapper sessionMapper;

    //CRUD operations
    @Transactional
    public SessionResponseDTO createSession(SessionRequestDTO sessionRequestDTO, UUID ownerId){

        if(sessionRequestDTO == null){
            throw new InvalidSessionDataException("Invalid session data");
        }

        if(hasActiveSession(ownerId)){
            throw new SessionAccessDeniedException("Cannot start a new session with a active session ");
        }

        if (sessionRequestDTO.getWorkDurationMinutes() < sessionProperties.getMinWorkDurationMinutes() ||
                sessionRequestDTO.getWorkDurationMinutes() > sessionProperties.getMaxWorkDurationMinutes()) {
            throw new InvalidSessionDataException("Work duration must be between " +
                    sessionProperties.getMinWorkDurationMinutes() + "-" +
                    sessionProperties.getMaxWorkDurationMinutes() + " minutes");
        }

        //Creates new session
        Session newSession = sessionMapper.fromRequestDTO(sessionRequestDTO);
        log.info("Creating new session {}"+" for user {}", newSession, ownerId);

        //Assigns creator user as the owner
        newSession.setOwnerUsername(getUsernameFromUserId(ownerId));

        //Declares when session was created
        newSession.setCreatedAt(LocalDateTime.now());
        newSession.setStartTime(LocalDateTime.now());

        //Pomodoro initialization
        newSession.setCurrentType(SessionType.WORK);
        newSession.setCurrentPhaseStartTime(LocalDateTime.now());
        newSession.setCurrentDurationMinutes(newSession.getWorkDurationMinutes());

        //Creating unique invite code for session
        newSession.setInviteCode(generateInviteCode());



        //Saves changes and changes session status to ACTIVE
        newSession.setStatus(SessionStatus.ACTIVE);
        Session savedSession = sessionRepository.save(newSession);
        log.info("Created session {}"+" with invite code {}", savedSession.getSessionId(), savedSession.getInviteCode());

        return sessionMapper.toResponseDTO(savedSession);
    }

    @Transactional
    public SessionResponseDTO updateSession(UUID sessionId, UpdateSessionRequestDTO request, UUID ownerId){
        log.info("Updating session {} by owner {}", sessionId, ownerId);

        // Find session and validate ownership
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(()->new SessionNotFoundException("Session not found"));
        
        validateOwnership(session, ownerId);
        validateUpdateRequest(request);
        
        // Update session fields
        updateSessionFields(session, request);

        Session updatedSession = sessionRepository.save(session);
        log.info("Successfully updated session {}", sessionId);
        
        return sessionMapper.toResponseDTO(updatedSession);
    }

    private void validateOwnership(Session session, UUID ownerId) {
        String ownerUsername = getUsernameFromUserId(ownerId);
        if (!session.getOwnerUsername().equals(ownerUsername)) {
            throw new SessionAccessDeniedException("Only session owner can update session");
        }
    }

    private void validateUpdateRequest(UpdateSessionRequestDTO request) {
        if (request.getWorkDurationMinutes() != null &&
                (request.getWorkDurationMinutes() < sessionProperties.getMinWorkDurationMinutes() ||
                        request.getWorkDurationMinutes() > sessionProperties.getMaxWorkDurationMinutes())) {
            throw new InvalidSessionDataException("Work duration must be between " +
                    sessionProperties.getMinWorkDurationMinutes() + "-" +
                    sessionProperties.getMaxWorkDurationMinutes() + " minutes");
        }
        
        if (request.getShortBreakMinutes() != null &&
                (request.getShortBreakMinutes() < sessionProperties.getMinShortBreakMinutes() ||
                        request.getShortBreakMinutes() > sessionProperties.getMaxShortBreakMinutes())) {
            throw new InvalidSessionDataException("Short break duration must be between " +
                    sessionProperties.getMinShortBreakMinutes() + "-" +
                    sessionProperties.getMaxShortBreakMinutes() + " minutes");
        }
        
        if (request.getLongBreakMinutes() != null &&
                (request.getLongBreakMinutes() < sessionProperties.getMinLongBreakMinutes() ||
                        request.getLongBreakMinutes() > sessionProperties.getMaxLongBreakMinutes())) {
            throw new InvalidSessionDataException("Long break duration must be between " +
                    sessionProperties.getMinLongBreakMinutes() + "-" +
                    sessionProperties.getMaxLongBreakMinutes() + " minutes");
        }
    }

    private void updateSessionFields(Session session, UpdateSessionRequestDTO request) {
        updateIfNotNull(request.getSessionName(), session::setSessionName);
        updateIfNotNull(request.getDescription(), session::setDescription);
        updateIfNotNull(request.getLongBreakMinutes(), session::setLongBreakMinutes);
        updateIfNotNull(request.getShortBreakMinutes(), session::setShortBreakMinutes);
        
        if (request.getWorkDurationMinutes() != null) {
            session.setWorkDurationMinutes(request.getWorkDurationMinutes());
            // Update current phase duration if currently in WORK phase
            if (session.getCurrentType() == SessionType.WORK) {
                session.setCurrentDurationMinutes(request.getWorkDurationMinutes());
            }
        }
    }

    private <T> void updateIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    @Transactional
    public void deleteSession(UUID sessionId, UUID ownerId){
        log.info("Deleting session {} by owner {}", sessionId, ownerId);
        
        // Find session and validate ownership
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException("Session not found"));
        
        validateOwnership(session, ownerId);
        
        // Check if session can be deleted (business rules)
        validateSessionDeletion(session);
        
        // Soft delete (set isDeleted = true)
        session.setIsDeleted(true);
        session.setUpdatedAt(LocalDateTime.now());
        
        sessionRepository.save(session);
        log.info("Successfully deleted session {}", sessionId);
    }
    
    private void validateSessionDeletion(Session session) {
        // Optional: Add business rules for deletion
        // For example, you might want to prevent deletion of active sessions
        // if (session.getStatus() == SessionStatus.ACTIVE) {
        //     throw new InvalidSessionDataException("Cannot delete active session. End session first.");
        // }
        
        // For now, allow deletion of any session
        log.debug("Session {} passed deletion validation", session.getSessionId());
    }




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
        try{
            getCurrentActiveSession(userId);
            return true;
        }
        catch(SessionException e){
            return false;
        }
    }

    public SessionResponseDTO getSessionByInviteCode(String inviteCode){
        log.info("Looking up session with invite code: {}", inviteCode);
        
        if(inviteCode == null || inviteCode.trim().isEmpty()){
            throw new InvalidSessionDataException("Invite code cannot be null or empty");
        }
        
        Session session = sessionRepository.findByInviteCode(inviteCode.trim())
            .orElseThrow(() -> new SessionNotFoundException("No active session found with invite code: " + inviteCode));
        
        log.info("Found session {} with invite code {}", session.getSessionId(), inviteCode);
        return sessionMapper.toResponseDTO(session);
    }

    //Session Lifecycle Management
    public SessionResponseDTO endSession(UUID sessionId, UUID userId, EndSessionRequestDTO endSessionRequestDTO){

    }
    public SessionResponseDTO resumeSession(UUID sessionId, UUID userId){

    }
    public SessionResponseDTO pauseSession(UUID sessionId, UUID userId){

    }
    public SessionResponseDTO extendSession(UUID sessionId, UUID userId, int addedTime){

    }

    //Participant Management
    public SessionResponseDTO inviteUser(UUID sessionId, UUID inviteeId, UUID inviterId){

    }
    public SessionResponseDTO removeUser(UUID sessionId, UUID userToRemove, UUID ownerId){

    }
    SessionResponseDTO joinSession(UUID sessionId, UUID userId, String inviteCode);
    void leaveSession(UUID sessionId, UUID userId);
    List<UUID> getSessionParticipants(UUID sessionId, UUID requesterId);
    private String generateInviteCode(){
        return UUID.randomUUID().toString().substring(0, 8);
    }

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
