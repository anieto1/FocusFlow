package com.pm.userservice.mapper;

import com.pm.userservice.dto.UserRequestDTO;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.model.User;

public class UserMapper {
    
    public static UserResponseDTO toDTO(User user) {
        if(user == null) {return null;}
        
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId() != null ? user.getId().toString():null);
        userResponseDTO.setFirstName(user.getFirstName());
        userResponseDTO.setLastName(user.getLastName());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setUsername(user.getUsername());
        userResponseDTO.setPassword(user.getPassword());
        userResponseDTO.setProfilePictureUrl(user.getProfilePictureUrl());
        return userResponseDTO;
        
        
    }

    public static User toModel(UserRequestDTO userRequestDTO) {
        if(userRequestDTO == null) {return null;}

        User user = new User();
        user.setFirstName(userRequestDTO.getFirstName());
        user.setLastName(userRequestDTO.getLastName());
        user.setEmail(userRequestDTO.getEmail());
        user.setUsername(userRequestDTO.getUsername());
        user.setProfilePictureUrl(userRequestDTO.getProfilePictureUrl());
        return user;
    }
}
