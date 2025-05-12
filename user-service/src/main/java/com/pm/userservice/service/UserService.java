package com.pm.userservice.service;

import com.pm.userservice.dto.UserRequestDTO;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.exception.EmailAlreadyExistsException;
import com.pm.userservice.exception.UserNotFoundException;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.model.User;
import com.pm.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository  =userRepository;
    }

    public List<UserResponseDTO> getUsers(){
        List<User> users = userRepository.findAll();
        return users.stream().map(UserMapper::toDTO).toList();

    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO){
        if(userRepository.existsByEmail(userRequestDTO.getEmail())){
            throw new EmailAlreadyExistsException(
                    "A user with this email already exists"+userRequestDTO.getEmail()
            );
        }

        User newUser = userRepository.save(
                UserMapper.toModel(userRequestDTO)
        );

        return UserMapper.toDTO(newUser);
    }

    public UserResponseDTO updateUser(UUID id, UserRequestDTO userRequestDTO){
        User user = userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User not found"));

        if(userRepository.existsByEmailAndIdNot(userRequestDTO.getEmail(), id)){
            throw new EmailAlreadyExistsException("A user with this email already exists");
        }

        user.setFirstName(userRequestDTO.getFirstName());
        user.setLastName(userRequestDTO.getLastName());
        user.setEmail(userRequestDTO.getEmail());
        user.setProfilePictureUrl(userRequestDTO.getProfilePictureUrl());
        User updateUser = userRepository.save(user);

        return UserMapper.toDTO((updateUser));

    }


}
