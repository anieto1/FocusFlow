package com.pm.sessionservice.Service;

import com.pm.sessionservice.DTO.SessionRequestDTO;
import com.pm.sessionservice.DTO.SessionResponseDTO;
import com.pm.sessionservice.model.Session;

import java.util.List;
import java.util.UUID;

public interface SessionService {
    SessionResponseDTO createSession(SessionRequestDTO request);
    SessionResponseDTO updateSession(UUID sessionId, SessionRequestDTO request);
    void deleteSession(UUID sessionId);

    List<SessionResponseDTO> getSessions();
    SessionResponseDTO getSession(UUID sessionId);

    SessionResponseDTO startSession(UUID sessionId);
    SessionResponseDTO stopSession(UUID sessionId);

    SessionResponseDTO addUserToSession(UUID sessionId, String userName);
    List<String> getUsersInSession(UUID sessionId);

    List<SessionResponseDTO> getSessionsByUser(UUID userId);
    List<SessionResponseDTO> getUpcomingSessions();
}
