package com.pm.sessionservice.Service.impl;


import com.pm.sessionservice.Config.SessionProperties;
import com.pm.sessionservice.DTO.*;
import com.pm.sessionservice.Exception.InvalidSessionDataException;
import com.pm.sessionservice.Exception.SessionAccessDeniedException;
import com.pm.sessionservice.Exception.SessionException;
import com.pm.sessionservice.Exception.SessionNotFoundException;
import com.pm.sessionservice.Mapper.SessionMapper;
import com.pm.sessionservice.Repository.SessionParticipantRepository;
import com.pm.sessionservice.Repository.SessionRepository;
import com.pm.sessionservice.Service.SessionService;
import com.pm.sessionservice.model.*;
import org.springframework.cglib.core.Local;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;


@RequiredArgsConstructor
@Service
public class SessionServiceImpl implements SessionService {
    private static final Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);
    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
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
        Session session = findSessionOrThrow(sessionId);
        
        validateOwnership(session, ownerId);
        validateUpdateRequest(request);
        
        // Update session fields
        updateSessionFields(session, request);

        Session updatedSession = sessionRepository.save(session);
        log.info("Successfully updated session {}", sessionId);
        
        return sessionMapper.toResponseDTO(updatedSession);
    }


    @Transactional
    public void deleteSession(UUID sessionId, UUID ownerId){
        log.info("Deleting session {} by owner {}", sessionId, ownerId);
        
        // Find session and validate ownership
        Session session = findSessionOrThrow(sessionId);
        
        validateOwnership(session, ownerId);
        
        // Check if session can be deleted (business rules)
        validateSessionDeletion(session);
        
        // Soft delete (set isDeleted = true)
        session.setIsDeleted(true);
        session.setUpdatedAt(LocalDateTime.now());
        
        sessionRepository.save(session);
        log.info("Successfully deleted session {}", sessionId);
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
    @Transactional
    public SessionResponseDTO endSession(UUID sessionId, UUID userId, EndSessionRequestDTO endSessionRequestDTO){
        log.info("Ending session {} by user {}", sessionId, userId);
        
        // Find session and validate ownership
        Session session = findSessionOrThrow(sessionId);
        
        validateOwnership(session, userId);
        
        // Validate session can be ended
        if (session.getStatus() == SessionStatus.COMPLETED || session.getStatus() == SessionStatus.CANCELLED) {
            throw new InvalidSessionDataException("Session is already completed or cancelled");
        }

        // Set final session state
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());

        // Calculate total session duration in minutes
        Duration sessionDuration = durationTime(session.getStartTime(), session.getEndTime());
        session.setTotalSessionDurationMinutes(sessionDuration.toMinutes());

        Session completedSession = sessionRepository.save(session);
        log.info("Successfully ended session {} with duration {} minutes", sessionId, sessionDuration.toMinutes());
        
        return sessionMapper.toResponseDTO(completedSession);
    }
    public SessionResponseDTO resumeSession(UUID sessionId, UUID userId){
        //Checks if session exists and if they are the owner
        log.info("Resuming session {} by user {}", sessionId, userId);
        Session session = findSessionOrThrow(sessionId);
        validateOwnership(session, userId);

        //Validate if session can be resumed
        if(session.getStatus() != SessionStatus.PAUSED){
            throw new InvalidSessionDataException("Session cannot be resumed");
        }

        //Set final session state
        session.setStatus(SessionStatus.ACTIVE);
        session.setCurrentPhaseStartTime(LocalDateTime.now());


        Session resumedSession = sessionRepository.save(session);
        return sessionMapper.toResponseDTO(resumedSession);
    }
    public SessionResponseDTO pauseSession(UUID sessionId, UUID userId){
        log.info("Paused session {} by user {}", sessionId, userId);
        Session session = findSessionOrThrow(sessionId);
        validateOwnership(session, userId);

        //Validate
        if(session.getStatus() != SessionStatus.ACTIVE){
            throw new InvalidSessionDataException("Session cannot be paused");
        }

        //Set final session rate
        session.setStatus(SessionStatus.PAUSED);
        session.setCurrentPhaseStartTime(LocalDateTime.now());

        Session pausedSession = sessionRepository.save(session);
        return sessionMapper.toResponseDTO(pausedSession);

    }

    //Participant Management
    public SessionResponseDTO inviteUser(UUID sessionId, UUID inviteeId, UUID inviterId){
        log.info("User {} requesting invite code for session {} to share with {}", inviterId, sessionId,
                inviteeId);

        // Find session and validate ownership
        Session session = findSessionOrThrow(sessionId);

        validateOwnership(session, inviterId);

        // Validate session is joinable
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new InvalidSessionDataException("Cannot invite to non-active session");
        }

        // Validate capacity - ensure session has room for additional participants
        if(session.getCurrentParticipantCount() >= session.getMaxParticipants()){
            throw new InvalidSessionDataException("Session is at maximum capacity (" + 
                session.getMaxParticipants() + " participants)");
        }

        log.info("Returning invite code {} for session {}", session.getInviteCode(), sessionId);

        // Return session with invite code - frontend handles sharing
        return sessionMapper.toResponseDTO(session);

    }
    @Transactional
    public SessionResponseDTO removeUser(UUID sessionId, UUID userToRemove, UUID ownerId){
        log.info("Removing  user {} from session {}", userToRemove, sessionId);
        Session session = findSessionOrThrow(sessionId);

        //Validation
        validateOwnership(session, ownerId);

        if(session.getStatus() != SessionStatus.ACTIVE){
            throw new InvalidSessionDataException("Session is not active");
        }

        if(userToRemove == null){
            throw new InvalidSessionDataException("User to remove is empty");
        }

        if(!sessionParticipantRepository.isUserActiveParticipant(sessionId, userToRemove)){
            throw new InvalidSessionDataException("User is not a participant in session");
        }

        sessionParticipantRepository.removeParticipantFromSession(sessionId, userToRemove, LocalDateTime.now());
        session.setCurrentParticipantCount(session.getCurrentParticipantCount()-1);

        Session removedUserSession = sessionRepository.save(session);

        return sessionMapper.toResponseDTO(removedUserSession);

    }

    @Transactional
    public SessionResponseDTO joinSession(UUID sessionId, UUID userId, String inviteCode){
        log.info("Joining session {} by user {}", sessionId, userId);
        //Null checks, invite code cleaning, and session availability
        if(sessionId == null || userId == null || inviteCode == null) {
            throw new InvalidSessionDataException("One or more required fields are empty");
        }
        String trimmedInviteCode = inviteCode.trim();
        Session session = findSessionOrThrow(sessionId);

        //Validating invite codes match
        if(!trimmedInviteCode.equalsIgnoreCase(session.getInviteCode())){
            throw new InvalidSessionDataException("Invite code is invalid");
        }
        //checks if session status is active
        if(!session.getStatus().equals(SessionStatus.ACTIVE)){
            throw new InvalidSessionDataException("Session is not active");
        }

        if(sessionParticipantRepository.isUserActiveParticipant(sessionId, userId)){
            throw new InvalidSessionDataException("User is already a participant in session");
        }

        if(sessionParticipantRepository.countActiveParticipantsBySessionId(sessionId) > sessionProperties.getMaxAllowedParticipants()){
            throw new SessionAccessDeniedException("Max allowed participants exceeded");
        }
        //TODO: Ensure user exists via gRPC call to user service

        // Create and save participant
        SessionParticipant participant = createParticipant(sessionId, userId);
        sessionParticipantRepository.save(participant);
        
        // Update session participant count
        session.setCurrentParticipantCount(session.getCurrentParticipantCount() + 1);
        Session updatedSession = sessionRepository.save(session);
        
        log.info("User {} successfully joined session {}", userId, sessionId);
        return sessionMapper.toResponseDTO(updatedSession);
    }
    public void leaveSession(UUID sessionId, UUID userId){

    }
    public List<UUID> getSessionParticipants(UUID sessionId, UUID requesterId){

    }

    //Permission and Access control
    public boolean isUserSessionOwner(UUID sessionId, UUID userId){

    }
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


    //Helper methods
    
    // Session lookup and validation helpers
    private Session findSessionOrThrow(UUID sessionId){
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found"));
    }
    
    private void validateOwnership(Session session, UUID ownerId) {
        String ownerUsername = getUsernameFromUserId(ownerId);
        if (!session.getOwnerUsername().equals(ownerUsername)) {
            throw new SessionAccessDeniedException("Only session owner can update session");
        }
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
    
    // Update request validation helpers
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
    
    // Session field update helpers
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
    
    // Utility and integration helpers
    private String getUsernameFromUserId(UUID userId) {
        // TODO: Replace with actual call to user-service
        // Example: return userServiceClient.getUserById(userId).getUsername();

        // For now, return a placeholder
        log.warn("Using placeholder username lookup for userId: {}", userId);
        return "user_" + userId.toString().substring(0, 8);
    }
    
    private Duration durationTime(LocalDateTime start, LocalDateTime end){
        if(start.isAfter(end)){
            throw new RuntimeException("start time cannot be after end time");
        }
        return Duration.between(start, end);
    }
    
    private String generateInviteCode(){
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    private SessionParticipant createParticipant(UUID sessionId, UUID userId) {
        SessionParticipant participant = new SessionParticipant();
        participant.setSessionId(sessionId);
        participant.setUserId(userId);
        participant.setJoinedAt(LocalDateTime.now());
        participant.setRole(ParticipantRole.PARTICIPANT);
        participant.setIsActive(true);
        participant.setCurrentSessionStartTime(LocalDateTime.now());
        participant.setIsCurrentlyInSession(true);
        participant.setTotalSessionTimeMinutes(0);
        participant.setWorkSessionsParticipated(0);
        return participant;
    }

}
