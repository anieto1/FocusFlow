package com.pm.sessionservice.Service.impl;


import com.pm.sessionservice.DTO.SessionRequestDTO;
import com.pm.sessionservice.DTO.SessionResponseDTO;
import com.pm.sessionservice.Mapper.SessionMapper;
import com.pm.sessionservice.Repository.SessionRepository;
import com.pm.sessionservice.model.Session;
import jakarta.websocket.SessionException;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceImpl {

    private final SessionRepository sessionRepository;

    public SessionServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public SessionResponseDTO createSession(SessionRequestDTO sessionRequestDTO) {
//        if(sessionRepository.existsBySessionId(sessionRequestDTO.getSessionId())){
//            throw new SessionException("A ")
//        }
        Session newSession = sessionRepository.save(SessionMapper.toSession(sessionRequestDTO));

        return SessionMapper.toSessionResponseDTO(newSession);

    }

}
