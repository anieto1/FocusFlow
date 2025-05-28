package com.pm.sessionservice.Mapper;

import com.pm.sessionservice.DTO.SessionResponseDTO;
import com.pm.sessionservice.model.Session;

public class SessionMapper {

    public static SessionResponseDTO toSessionResponseDTO(Session session) {
        if(session == null){ return null;}

        SessionResponseDTO sessionResponseDTO = new SessionResponseDTO();
        sessionResponseDTO.setSessionId(session.getSessionId());
        sessionResponseDTO.setOwnerUsername(session.getOwnerUsername());
        sessionResponseDTO.setUserIds(session.getUserIds());
        sessionResponseDTO.setStartTime(session.getStartTime());
        sessionResponseDTO.setEndTime(session.getEndTime());
        sessionResponseDTO.setScheduledTime(session.getScheduledTime());
        sessionResponseDTO.setCreatedAt(session.getCreatedAt());
        return sessionResponseDTO;
    }

    public static Session toSession(SessionResponseDTO sessionResponseDTO) {
        if(sessionResponseDTO == null){ return null;}

        Session session = new Session();
        session.setOwnerUsername(sessionResponseDTO.getOwnerUsername());
        session.setUserIds(sessionResponseDTO.getUserIds());
        session.setStartTime(sessionResponseDTO.getStartTime());
        session.setEndTime(sessionResponseDTO.getEndTime());
        session.setScheduledTime(sessionResponseDTO.getScheduledTime());
        return session;
    }
}
