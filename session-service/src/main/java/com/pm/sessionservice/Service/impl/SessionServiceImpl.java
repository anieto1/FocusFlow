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

import java.time.LocalDateTime;
import java.util.UUID;
@RequiredArgsConstructor
@Service
public class SessionServiceImpl{
    private static final Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);
    private final SessionRepository sessionRepository;
    private final SessionProperties sessionProperties;
    private SessionMapper sessionMapper;


    //@Override
    @Transactional
    public SessionResponseDTO createSession(SessionResponseDTO request, UUID ownerId){
        log.info("Creating new session for owner id {}", ownerId);
        Session session = new Session();

        return sessionMapper.toResponseDTO(session);
    }

}
