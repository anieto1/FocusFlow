package com.pm.sessionservice.Service;

import com.pm.sessionservice.DTO.SessionRequestDTO;
import com.pm.sessionservice.DTO.SessionResponseDTO;
import com.pm.sessionservice.model.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SessionService {

    //CRUD operations
    SessionResponseDTO createSession(SessionRequestDTO sessionRequestDTO);
    SessionResponseDTO getSessionById(UUID sessionId, String userId);
    SessionResponseDTO updateSession(UUID sessionId, SessionRequestDTO request);
    void deleteSession(UUID sessionId);

    //Query Methods
    Page<SessionResponseDTO> getSessionsByUser(String userId, Pageable pageable);
    Page<SessionResponseDTO> getUpcomingSessions(String userId, Pageable pageable);
    Page<SessionResponseDTO> getActiveSessionsByUser(String userId, Pageable pageable);
    List<SessionResponseDTO> getSessionsByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);

    //Session Lifecycle Management
    SessionResponseDTO startSession(UUID sessionId, String userId);
    SessionResponseDTO stopSession(UUID sessionId, String userId);
    SessionResponseDTO resumeSession(UUID sessionId, String userId);
    SessionResponseDTO pauseSession(UUID sessionId, String userId);
    SessionResponseDTO extendSession(UUID sessionId, String userId, int addedTime);

    //Participant Management
    SessionResponseDTO inviteUser(UUID sessionId, String userId);
    SessionResponseDTO removeUser(UUID sessionId, String userId);
    void leaveSession(UUID sessionId, String userId);
    List<SessionResponseDTO> getSessionUsers(UUID sessionId);

    //Permission and Access control
    boolean isUserSessionOwner(UUID sessionId, String userId);
















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
