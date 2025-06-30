package com.pm.sessionservice.Service.impl;


import com.pm.sessionservice.Config.SessionProperties;
import com.pm.sessionservice.DTO.SessionRequestDTO;
import com.pm.sessionservice.DTO.SessionResponseDTO;
import com.pm.sessionservice.Exception.InvalidSessionDataException;
import com.pm.sessionservice.Exception.SessionException;
import com.pm.sessionservice.Mapper.SessionMapper;
import com.pm.sessionservice.Repository.SessionRepository;
import com.pm.sessionservice.model.Session;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDateTime;
import java.util.UUID;
@RequiredArgsConstructor
@Service
public class SessionServiceImpl{
    private static final Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);
    private final SessionRepository sessionRepository;
    private final SessionProperties sessionProperties;
    private SessionMapper sessionMapper;


    //CRUD operations
    @Transactional
    public SessionResponseDTO createSession(SessionRequestDTO request, UUID ownerId){
        log.info("Creating new session for owner id {}", ownerId);
        Session newSession = sessionRepository.save(sessionMapper.fromRequestDTO(request));
        return sessionMapper.toResponseDTO(newSession);
    }

    @Transactional
    public SessionResponseDTO updateSession(SessionRequestDTO request, UUID ownerId){
        Session session = sessionRepository.findById(ownerId).orElseThrow(()-> new SessionException("Session not found"));
        log.info("Updating session for owner id {}", ownerId);

        session.setSessionName(request.getSessionName());
        session.setDescription(request.getDescription());
        session.setScheduledTime(request.getScheduledTime());


        return sessionMapper.toResponseDTO(sessionRepository.save(session));
    }

    @DeleteMapping
    public void deleteSession(UUID ownerId){
        log.info("Deleting session for owner id {}", ownerId);
        if(sessionRepository.existsById(ownerId)){
            throw new SessionException("Session not found");
        }
        sessionRepository.deleteById(ownerId);
    }


}
