package com.pm.sessionservice.Mapper;

import com.pm.sessionservice.DTO.SessionRequestDTO;
import com.pm.sessionservice.DTO.SessionResponseDTO;
import com.pm.sessionservice.model.Session;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    SessionResponseDTO toResponseDTO(Session session);
    Session fromRequestDTO(SessionRequestDTO sessionRequestDTO);


//    public static SessionResponseDTO toSessionResponseDTO(Session session) {
//        if(session == null){ return null;}
//
//        SessionResponseDTO sessionResponseDTO = new SessionResponseDTO();
//        sessionResponseDTO.setSessionId(session.getSessionId());
//        sessionResponseDTO.setOwnerUsername(session.getOwnerUsername());
//        sessionResponseDTO.setSessionName(session.getSessionName());
//        sessionResponseDTO.setUserIds(session.getUserIds());
//        sessionResponseDTO.setStartTime(session.getStartTime());
//        sessionResponseDTO.setEndTime(session.getEndTime());
//        sessionResponseDTO.setScheduledTime(session.getScheduledTime());
//        sessionResponseDTO.setCreatedAt(session.getCreatedAt());
//        sessionResponseDTO.setStatus(session.getStatus());
//        return sessionResponseDTO;
//    }
//
//    public static Session toSession(SessionRequestDTO sessionRequestDTO) {
//        if(sessionRequestDTO == null){ return null;}
//
//        Session session = new Session();
//        session.setSessionId(sessionRequestDTO.getSessionId());
//        session.setOwnerUsername(sessionRequestDTO.getOwnerUsername());
//        session.setSessionName(sessionRequestDTO.getSessionName());
//        session.setUserIds(sessionRequestDTO.getUserIds());
//        session.setStartTime(sessionRequestDTO.getStartTime());
//        session.setEndTime(sessionRequestDTO.getEndTime());
//        session.setScheduledTime(sessionRequestDTO.getScheduledTime());
//        session.setStatus(sessionRequestDTO.getStatus());
//        return session;
//    }
}
