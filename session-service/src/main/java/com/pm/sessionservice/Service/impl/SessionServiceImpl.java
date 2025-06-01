package com.pm.sessionservice.Service.impl;


import com.pm.sessionservice.DTO.SessionRequestDTO;
import com.pm.sessionservice.DTO.SessionResponseDTO;
import com.pm.sessionservice.Exception.SessionException;
import com.pm.sessionservice.Mapper.SessionMapper;
import com.pm.sessionservice.Repository.SessionRepository;
import com.pm.sessionservice.model.Session;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SessionServiceImpl {

    private final SessionRepository sessionRepository;

    public SessionServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }


    public SessionResponseDTO createSession(SessionRequestDTO sessionRequestDTO){
        Session newSession = sessionRepository.save(SessionMapper.toSession(sessionRequestDTO));
        return SessionMapper.toSessionResponseDTO(newSession);
    }

    public SessionResponseDTO updateSession(UUID id,SessionRequestDTO sessionRequestDTO){
        Session session = sessionRepository.findById(id).orElse(null);

        if(session != null){
            session.setSessionName(sessionRequestDTO.getSessionName());
            session.setStatus(sessionRequestDTO.getStatus());
            session.setStartTime(sessionRequestDTO.getStartTime());
            session.setEndTime(sessionRequestDTO.getEndTime());
            session.setScheduledTime(sessionRequestDTO.getScheduledTime());

        }
        Session updatedSession = sessionRepository.save(session);
        return SessionMapper.toSessionResponseDTO(updatedSession);
    }

    public void deleteSession(UUID id){
        if(!sessionRepository.existsById(id)){
            throw new SessionException("Session not found with id: "+id);
        }
        sessionRepository.deleteById(id);
    }

}
