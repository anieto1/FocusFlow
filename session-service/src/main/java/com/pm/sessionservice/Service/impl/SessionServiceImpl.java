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
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


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
    @Transactional
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
    @Transactional
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
    @Transactional
    public void leaveSession(UUID sessionId, UUID userId){
        //Null checker and session finder
        log.info("User {} is leaving session {}", userId, sessionId);
        if(sessionId ==null || userId == null){
            throw new InvalidSessionDataException("One or more required fields are empty");
        }
        Session session = findSessionOrThrow(sessionId);

        //Validation
        if(!session.getStatus().equals(SessionStatus.ACTIVE)){
            throw new InvalidSessionDataException("Session is not active");
        }

        // Prevent owner from leaving (they should delete session instead)
        if(getUsernameFromUserId(userId).equals(session.getOwnerUsername())){
            throw new SessionAccessDeniedException("Session owner cannot leave - delete session instead");
        }

        if(!sessionParticipantRepository.isUserActiveParticipant(sessionId, userId)){
            throw new InvalidSessionDataException("User is not a participant in session");
        }
        
        // Check if leaving would go below minimum participants
        if(sessionParticipantRepository.countActiveParticipantsBySessionId(sessionId) <= sessionProperties.getMinAllowedParticipants()){
            throw new SessionAccessDeniedException("Cannot leave session - would go below minimum required participants");
        }

        sessionParticipantRepository.removeParticipantFromSession(sessionId, userId, LocalDateTime.now());
        session.setCurrentParticipantCount(session.getCurrentParticipantCount() - 1);
        sessionRepository.save(session);
        
        log.info("User {} successfully left session {}", userId, sessionId);
    }
    @Transactional(readOnly = true)
    public List<UUID> getSessionParticipants(UUID sessionId, UUID requesterId){
        log.info("Getting participants for session {} requested by user {}", sessionId, requesterId);
        if(sessionId == null || requesterId == null){
            throw new InvalidSessionDataException("One or more required fields are empty");
        }
        
        Session session = findSessionOrThrow(sessionId);
        String requesterUsername = getUsernameFromUserId(requesterId);
        
        // Check if requester is either session owner OR active participant
        boolean isOwner = session.getOwnerUsername().equals(requesterUsername);
        boolean isParticipant = sessionParticipantRepository.isUserActiveParticipant(sessionId, requesterId);
        
        if(!isOwner && !isParticipant){
            throw new SessionAccessDeniedException("Access denied - user must be session owner or participant");
        }

        List<UUID> participantIds = sessionParticipantRepository.findActiveParticipantUserIds(sessionId);
        log.info("Found {} participants in session {}", participantIds.size(), sessionId);
        
        return participantIds;
    }

    //Permission and Access control
    public boolean isUserSessionOwner(UUID sessionId, UUID userId){
        log.info("Checking if user {} is owner of session {}", userId, sessionId);
        
        if(sessionId == null || userId == null){
            return false;
        }
        
        try {
            Session session = findSessionOrThrow(sessionId);
            String userUsername = getUsernameFromUserId(userId);
            boolean isOwner = session.getOwnerUsername().equals(userUsername);
            
            log.debug("User {} ownership check for session {}: {}", userId, sessionId, isOwner);
            return isOwner;
        } catch (Exception e) {
            log.warn("Error checking ownership for user {} and session {}: {}", userId, sessionId, e.getMessage());
            return false;
        }
    }

    public boolean canUserJoinSession(UUID sessionId, UUID userId, String inviteCode){
        log.info("Checking if user {} can join session {}", userId, sessionId);
        if(userId == null || sessionId == null || inviteCode == null){
            return false;
        }
        
        try {
            String cleanInviteCode = inviteCode.trim();
            Session session = findSessionOrThrow(sessionId);

            if(!session.getStatus().equals(SessionStatus.ACTIVE)){
                return false;
            }

            if(sessionParticipantRepository.isUserActiveParticipant(sessionId, userId)){
                return false;
            }

            // Check if session has capacity for 1 additional participant
            validateSessionCapacity(sessionId, 1);

            return session.getInviteCode().equalsIgnoreCase(cleanInviteCode);
        } catch (Exception e) {
            log.debug("User {} cannot join session {} due to: {}", userId, sessionId, e.getMessage());
            return false;
        }
    }


    // Validation & Business Rules
    public void validateSessionCapacity(UUID sessionId, int additionalParticipants){
        Session session = findSessionOrThrow(sessionId);
        int currentCount = session.getCurrentParticipantCount();
        
        if(currentCount + additionalParticipants > session.getMaxParticipants()){
            throw new InvalidSessionDataException("Session is at maximum capacity (" + 
                session.getMaxParticipants() + " participants). Cannot add " + additionalParticipants + " more.");
        }
    }

    //Pomodoro Phase Management
    @Transactional
    public SessionResponseDTO startWorkPhase(UUID sessionId, UUID userId){
        log.info("Starting work phase for session {}", sessionId);
        if(sessionId == null || userId == null){
            throw new InvalidSessionDataException("One or more required fields are empty");
        }
        Session session = findSessionOrThrow(sessionId);
        validateOwnership(session, userId);
        if(!session.getStatus().equals(SessionStatus.ACTIVE)){
            throw new InvalidSessionDataException("Session is not active");
        }
        session.setCurrentType(SessionType.WORK);
        session.setCurrentPhaseStartTime(LocalDateTime.now());
        session.setCurrentDurationMinutes(session.getWorkDurationMinutes());
        Session savedSession = sessionRepository.save(session);
        return sessionMapper.toResponseDTO(savedSession);
    }

    @Transactional
    public SessionResponseDTO startBreakPhase(UUID sessionId, UUID userId, SessionType breakType){
        log.info("Starting break phase for session {} with type {}", sessionId, breakType);
        
        // Input validation
        if(sessionId == null || userId == null || breakType == null){
            throw new InvalidSessionDataException("One or more required fields are empty");
        }
        
        Session session = findSessionOrThrow(sessionId);
        validateOwnership(session, userId);
        
        if(!session.getStatus().equals(SessionStatus.ACTIVE)){
            throw new InvalidSessionDataException("Session is not active");
        }
        
        // Validate breakType is actually a break type
        if(breakType != SessionType.SHORT_BREAK && breakType != SessionType.LONG_BREAK) {
            throw new InvalidSessionDataException("Invalid break type. Must be SHORT_BREAK or LONG_BREAK");
        }
        
        // Set duration based on break type
        int breakDuration = (breakType == SessionType.SHORT_BREAK) 
            ? session.getShortBreakMinutes() 
            : session.getLongBreakMinutes();
        
        session.setCurrentType(breakType);
        session.setCurrentPhaseStartTime(LocalDateTime.now());
        session.setCurrentDurationMinutes(breakDuration);
        
        Session savedSession = sessionRepository.save(session);
        log.info("Successfully started {} for session {} with duration {} minutes", 
                breakType, sessionId, breakDuration);
        
        return sessionMapper.toResponseDTO(savedSession);
    }
    @Transactional
    public SessionResponseDTO completeWorkPhase(UUID sessionId, UUID userId){
        log.info("Completing work phase for session {}", sessionId);
        if(sessionId == null || userId == null){
            throw new InvalidSessionDataException("One or more required fields are empty");
        }

        Session session = findSessionOrThrow(sessionId);
        validateOwnership(session, userId);

        //Increments total work session completed
        session.setTotalWorkSessionsCompleted(session.getTotalWorkSessionsCompleted()+1);
        
        // Save the completed work session count first
        sessionRepository.save(session);

        log.info("Work phase completed, starting new work phase for session {}", sessionId);
        // Start new work phase (this method already saves and returns the DTO)
        return startWorkPhase(sessionId, userId);
    }
    @Transactional
    public SessionResponseDTO skipBreak(UUID sessionId, UUID userId){
        log.info("Skipping break phase for session {}", sessionId);
        
        // Input validation
        if(sessionId == null || userId == null){
            throw new InvalidSessionDataException("One or more required fields are empty");
        }

        Session session = findSessionOrThrow(sessionId);
        validateOwnership(session, userId);

        log.info("Break phase skipped, starting work phase for session {}", sessionId);
        // Start work phase (this method already saves and returns the DTO)
        return startWorkPhase(sessionId, userId);
    }

    @Transactional(readOnly = true)
    public SessionProgressDTO getSessionProgress(UUID sessionId, UUID userId){
        log.info("Getting session progress for session {}", sessionId);
        
        // Input validation
        if (sessionId == null || userId == null) {
            throw new InvalidSessionDataException("Session ID and User ID cannot be null");
        }
        
        Session session = findSessionOrThrow(sessionId);

        // Access control - owner OR participant can view progress
        boolean isOwner = isUserSessionOwner(sessionId, userId);
        boolean isUserParticipant = sessionParticipantRepository.isUserActiveParticipant(sessionId, userId);

        if(!isOwner && !isUserParticipant){
            throw new SessionAccessDeniedException("Access denied: User must be owner or session participant");
        }

        // Use MapStruct for basic field mapping, then add calculated fields
        SessionProgressDTO progress = sessionMapper.toProgressDTO(session);
        
        // Add calculated fields that require business logic
        progress.setElapsedTime(calculateTotalElapsedTime(session));
        progress.setTimeRemainingInPhase(calculateTimeRemainingInPhase(session));
        
        // Task progress using helper methods
        List<UUID> taskIds = session.getTaskIds();
        progress.setTotalTasks(taskIds != null ? taskIds.size() : 0);
        progress.setTasksCompleted(calculateCompletedTasksCount(taskIds));
        progress.setCompletedTaskIds(new ArrayList<>()); // TODO: Get from task service via gRPC
        
        // Participant info using helper methods
        progress.setActiveParticipants(getActiveParticipantIds(sessionId));
        
        // Break selection status using helper methods
        progress.setIsWaitingForBreakSelection(isWaitingForBreakSelection(session));
        
        log.info("Successfully retrieved progress for session {} - {} elapsed, {} remaining in phase", 
                sessionId, progress.getElapsedTime(), progress.getTimeRemainingInPhase());
        
        return progress;
    }
    @Transactional(readOnly = true)
    public BreakSessionDTO getBreakOptions(UUID sessionId, UUID userId){
        log.info("Getting break options for session {} by user {}", sessionId, userId);
        
        // Input validation
        if (sessionId == null || userId == null) {
            throw new InvalidSessionDataException("Session ID and User ID cannot be null");
        }
        
        Session session = findSessionOrThrow(sessionId);
        
        // Access control - owner OR participant can view break options
        boolean isOwner = isUserSessionOwner(sessionId, userId);
        boolean isUserParticipant = sessionParticipantRepository.isUserActiveParticipant(sessionId, userId);

        if(!isOwner && !isUserParticipant){
            throw new SessionAccessDeniedException("Access denied: User must be owner or session participant");
        }
        
        // Use MapStruct for basic field mapping, then add calculated fields
        BreakSessionDTO breakOptions = sessionMapper.toBreakSessionDTO(session);
        
        // Add calculated fields that require business logic
        List<UUID> taskIds = session.getTaskIds();
        breakOptions.setTasks(taskIds != null ? taskIds.size() : 0);
        breakOptions.setTimeRemaining(calculateTimeRemainingInPhase(session));
        
        log.info("Break options retrieved for session {} - {} work sessions completed", 
                sessionId, breakOptions.getWorkSessionsCompleted());
        
        return breakOptions;
    }

    //Task Management within Sessions
    @Transactional
    public SessionResponseDTO addTaskToSession(UUID sessionId, UUID taskId, UUID userId){
        log.info("Adding task {} to session {} by user {}", taskId, sessionId, userId);
        
        // Input validation
        if (sessionId == null || taskId == null || userId == null) {
            throw new InvalidSessionDataException("Session ID, Task ID, and User ID cannot be null");
        }
        
        Session session = findSessionOrThrow(sessionId);
        validateOwnership(session, userId); // Only owners can manage tasks
        
        // Check if task is already in session
        List<UUID> taskIds = session.getTaskIds();
        if (taskIds.contains(taskId)) {
            throw new InvalidSessionDataException("Task is already associated with this session");
        }
        
        // TODO: Validate task exists via gRPC call to task service
        // Example: taskServiceClient.validateTaskExists(taskId, userId);
        
        // Add task to session
        taskIds.add(taskId);
        session.setTaskIds(taskIds);
        
        Session updatedSession = sessionRepository.save(session);
        log.info("Successfully added task {} to session {}", taskId, sessionId);
        
        return sessionMapper.toResponseDTO(updatedSession);
    }
    
    @Transactional
    public SessionResponseDTO removeTaskFromSession(UUID sessionId, UUID taskId, UUID userId){
        log.info("Removing task {} from session {} by user {}", taskId, sessionId, userId);
        
        // Input validation
        if (sessionId == null || taskId == null || userId == null) {
            throw new InvalidSessionDataException("Session ID, Task ID, and User ID cannot be null");
        }
        
        Session session = findSessionOrThrow(sessionId);
        validateOwnership(session, userId); // Only owners can manage tasks
        
        // Check if task is in session
        List<UUID> taskIds = session.getTaskIds();
        if (!taskIds.contains(taskId)) {
            throw new InvalidSessionDataException("Task is not associated with this session");
        }
        
        // Remove task from session
        taskIds.remove(taskId);
        session.setTaskIds(taskIds);
        
        Session updatedSession = sessionRepository.save(session);
        log.info("Successfully removed task {} from session {}", taskId, sessionId);
        
        return sessionMapper.toResponseDTO(updatedSession);
    }
    
    @Transactional
    public SessionResponseDTO markTaskCompleted(UUID sessionId, UUID taskId, UUID userId){
        log.info("Marking task {} as completed in session {} by user {}", taskId, sessionId, userId);
        
        // Input validation
        if (sessionId == null || taskId == null || userId == null) {
            throw new InvalidSessionDataException("Session ID, Task ID, and User ID cannot be null");
        }
        
        Session session = findSessionOrThrow(sessionId);
        
        // Access control - owner OR participant can complete tasks
        boolean isOwner = isUserSessionOwner(sessionId, userId);
        boolean isUserParticipant = sessionParticipantRepository.isUserActiveParticipant(sessionId, userId);

        if(!isOwner && !isUserParticipant){
            throw new SessionAccessDeniedException("Access denied: User must be owner or session participant");
        }
        
        // Check if task is in session
        List<UUID> taskIds = session.getTaskIds();
        if (!taskIds.contains(taskId)) {
            throw new InvalidSessionDataException("Task is not associated with this session");
        }
        
        // TODO: Mark task as completed via gRPC call to task service
        // Example: taskServiceClient.markTaskCompleted(taskId, userId);
        log.info("Task completion will be handled by task service via gRPC - session association maintained");
        
        // Session doesn't change - task completion is handled by task service
        // We just validate that the task belongs to this session
        log.info("Task {} completion request validated for session {}", taskId, sessionId);
        
        return sessionMapper.toResponseDTO(session);
    }
    
    @Transactional(readOnly = true)
    public List<UUID> getSessionTasks(UUID sessionId, UUID userId){
        log.info("Getting tasks for session {} by user {}", sessionId, userId);
        
        // Input validation
        if (sessionId == null || userId == null) {
            throw new InvalidSessionDataException("Session ID and User ID cannot be null");
        }
        
        Session session = findSessionOrThrow(sessionId);
        
        // Access control - owner OR participant can view tasks
        boolean isOwner = isUserSessionOwner(sessionId, userId);
        boolean isUserParticipant = sessionParticipantRepository.isUserActiveParticipant(sessionId, userId);

        if(!isOwner && !isUserParticipant){
            throw new SessionAccessDeniedException("Access denied: User must be owner or session participant");
        }
        
        List<UUID> taskIds = session.getTaskIds();
        log.info("Retrieved {} tasks for session {}", taskIds != null ? taskIds.size() : 0, sessionId);
        
        return taskIds != null ? new ArrayList<>(taskIds) : new ArrayList<>();
    }


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
        // Use MapStruct for automatic mapping of non-null values
        sessionMapper.updateSessionFromRequest(request, session);
        
        // Handle special case: Update current phase duration if currently in WORK phase
        if (request.getWorkDurationMinutes() != null && session.getCurrentType() == SessionType.WORK) {
            session.setCurrentDurationMinutes(request.getWorkDurationMinutes());
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

    //Time calculations
    private Duration calculateTotalElapsedTime(Session session){
        if(session.getStartTime()==null){
            return Duration.ZERO;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = session.getStartTime();
        return durationTime(startTime, now);
    }
    private Duration calculateTimeRemainingInPhase(Session session){
        LocalDateTime phaseStartTime = session.getCurrentPhaseStartTime();
        int phaseDurationMinutes = session.getCurrentDurationMinutes();

        Duration elapsedInPhase = durationTime(phaseStartTime, LocalDateTime.now());

        Duration totalPhaseTime = Duration.ofMinutes(phaseDurationMinutes);
        Duration remaining = totalPhaseTime.minus(elapsedInPhase);

        return remaining.isNegative() ? Duration.ZERO : remaining;

    }
    private boolean isPhaseOvertime(Session session){
        Duration remaining = calculateTimeRemainingInPhase(session);
        return remaining.equals(Duration.ZERO);
    }


    // Task progress helpers
    private int calculateCompletedTasksCount(List<UUID> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return 0;
        }
        
        // TODO: Replace with gRPC call to task service when task service is complete
        // Example: return taskServiceClient.getCompletedTaskCount(taskIds);
        // For now, we can't determine completion status from session service
        log.debug("Task completion count requested for {} tasks - returning 0 until task service integration", taskIds.size());
        
        return 0; // Will be replaced with actual gRPC call
    }
    
    private boolean isWaitingForBreakSelection(Session session) {
        if (session == null || session.getCurrentType() == null) {
            return false;
        }
        
        // User should select break type when:
        // 1. Currently in a WORK phase
        // 2. Work phase time has expired (overtime)
        boolean isWorkPhase = session.getCurrentType() == SessionType.WORK;
        boolean isOvertime = isPhaseOvertime(session);
        
        return isWorkPhase && isOvertime;
    }

    // Participant helpers
    private List<UUID> getActiveParticipantIds(UUID sessionId) {
        if (sessionId == null) {
            return new ArrayList<>();
        }
        
        // Reuse existing repository method - already implemented and tested
        return sessionParticipantRepository.findActiveParticipantUserIds(sessionId);
    }


}
