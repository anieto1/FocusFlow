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
import java.util.UUID;
@RequiredArgsConstructor
@Service
public class SessionServiceImpl implements SessionService {
    private static final Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);
    private final SessionRepository sessionRepository;
    private final SessionProperties sessionProperties;
    private final SessionMapper sessionMapper;


    //CRUD operations
    @Transactional
    public SessionResponseDTO createSession(SessionRequestDTO request, UUID ownerId){
        log.info("Creating new session for owner id {}", ownerId);
        Session newSession = sessionRepository.save(sessionMapper.fromRequestDTO(request));
        return sessionMapper.toResponseDTO(newSession);
    }

    @Transactional(readOnly = true)
    public SessionResponseDTO getSessionById(UUID sessionId, UUID userId){
        log.info("Retrieving session {} for user {}", sessionId, userId);
        
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionException("Session not found with id: " + sessionId));
        
        // Light validation - only check if session is available
        if (session.getIsDeleted()) {
            throw new SessionException("Session no longer available");
        }
        
        return sessionMapper.toResponseDTO(session);
    }

    @Transactional
    public SessionResponseDTO updateSession(UUID sessionId, UpdateSessionRequestDTO request, UUID ownerId){
        Session session = sessionRepository.findById(sessionId).orElseThrow(()-> new SessionException("Session not found"));
        log.info("Updating session for owner id {}", ownerId);

        session.setSessionName(request.getSessionName());
        session.setDescription(request.getDescription());
        session.setScheduledTime(request.getScheduledTime());


        return sessionMapper.toResponseDTO(sessionRepository.save(session));
    }

    @Transactional
    public void deleteSession(UUID sessionId, UUID ownerId){
        log.info("Deleting session for owner id {}", ownerId);
        if(!sessionRepository.existsById(sessionId)){
            throw new SessionException("Session not found");
        }
        sessionRepository.deleteById(sessionId);
    }

    //Query Methods
    @Transactional(readOnly = true)
    public Page<SessionSummaryDTO> getSessionsByUser(UUID userId, Pageable pageable){
        log.info("Fetching sessions for user: {}", userId);
        
        // TODO: In a real microservices setup, call user-service to get username
        // For now, we'll use a placeholder method
        String username = getUsernameFromUserId(userId);
        
        Page<Session> sessions = sessionRepository.findSessionsByUserInvolved(username, userId, pageable);
        return sessions.map(sessionMapper::toSummaryDTO);
    }

    // Helper method to get username from userId
    // In a real microservices setup, this would call user-service
    private String getUsernameFromUserId(UUID userId) {
        // TODO: Replace with actual call to user-service
        // Example: return userServiceClient.getUserById(userId).getUsername();
        
        // For now, return a placeholder
        log.warn("Using placeholder username lookup for userId: {}", userId);
        return "user_" + userId.toString().substring(0, 8);
    }

    // Helper method to check if user can access a session
    private boolean canUserAccessSession(Session session, UUID userId) {
        // Check if user is the owner
        String username = getUsernameFromUserId(userId);
        if (session.getOwnerUsername().equals(username)) {
            return true;
        }
        
        // Check if user is a participant
        return isUserParticipant(session.getSessionId(), userId);
    }

    // Helper method to check if user is a participant in the session
    private boolean isUserParticipant(UUID sessionId, UUID userId) {
        // TODO: Query session_participants table
        // For now, return true (will implement when we add participant repository)
        return true;
    }

    @Transactional(readOnly = true)
    public Page<SessionSummaryDTO> getActiveSessionsByUser(UUID userId, Pageable pageable){
        log.info("Fetching active sessions for user: {}", userId);
        
        String username = getUsernameFromUserId(userId);
        
        Page<Session> activeSessions = sessionRepository.findActiveSessionsByUserInvolved(
                username, userId, pageable);
                
        return activeSessions.map(sessionMapper::toSummaryDTO);
    }
    @Transactional(readOnly = true)
    public List<SessionResponseDTO> getSessionsByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate){
        log.info("Fetching sessions for user: {} within dates: {} - {}", userId, startDate, endDate);

        if (startDate.isAfter(endDate)) {
            throw new InvalidSessionDataException("Start date cannot be after end date");
        }
        String username = getUsernameFromUserId(userId);
        List<Session> sessions = sessionRepository.findSessionsByDateRangeInvolved(
                username, userId, startDate, endDate);
        
        return sessions.stream()
                .map(sessionMapper::toResponseDTO)
                .toList();
    }

    //Session Lifecycle Management
    SessionResponseDTO startSession(UUID sessionId, UUID userId){

    }
    SessionResponseDTO endSession(UUID sessionId, UUID userId, EndSessionRequestDTO endSessionRequestDTO){

    }
    SessionResponseDTO resumeSession(UUID sessionId, UUID userId){

    }
    SessionResponseDTO pauseSession(UUID sessionId, UUID userId){

    }
    SessionResponseDTO extendSession(UUID sessionId, UUID userId, int addedTime){

    }

    //Participant Management
    SessionResponseDTO inviteUser(UUID sessionId, UUID inviteeId, UUID inviterId){

    }
    SessionResponseDTO removeUser(UUID sessionId, UUID userToRemove, UUID ownerId){

    }
    SessionResponseDTO joinSession(UUID sessionId, UUID userId, String inviteCode){

    }
    void leaveSession(UUID sessionId, UUID userId){

    }
    List<UUID> getSessionParticipants(UUID sessionId, UUID requesterId){

    }
    String generateInviteCode(UUID sessionId, UUID ownerId){

    }

    //Permission and Access control
    boolean isUserSessionOwner(UUID sessionId, UUID userId){

    }
    boolean canUserJoinSession(UUID sessionId, UUID userId, String inviteCode){

    }

    // Validation & Business Rules
    void validateSessionTiming(LocalDateTime startTime, LocalDateTime endTime){

    }
    void validateSessionCapacity(UUID sessionId, int additionalParticipants){

    }
    boolean isSessionTimeSlotAvailable(UUID userId, LocalDateTime startTime, LocalDateTime endTime){

    }

    //Pomodoro Phase Management
    SessionResponseDTO startWorkPhase(UUID sessionId, UUID userId){

    }
    SessionResponseDTO startBreakPhase(UUID sessionId, UUID userId, SessionType breakType){

    }
    SessionResponseDTO completeWorkPhase(UUID sessionId, UUID userId){

    }
    SessionResponseDTO skipBreak(UUID sessionId, UUID userId){

    }
    SessionProgressDTO getSessionProgress(UUID sessionId, UUID userId){

    }
    BreakSessionDTO getBreakOptions(UUID sessionId, UUID userId){

    }

    //Task Management within Sessions
    SessionResponseDTO addTaskToSession(UUID sessionId, UUID taskId, UUID userId){

    }
    SessionResponseDTO removeTaskFromSession(UUID sessionId, UUID taskId, UUID userId){

    }
    SessionResponseDTO markTaskCompleted(UUID sessionId, UUID taskId, UUID userId){

    }
    List<UUID> getSessionTasks(UUID sessionId, UUID userId){

    }



}
