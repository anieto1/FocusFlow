package com.pm.userservice.mapper;

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
        userResponseDTO.setProfilePicture(user.getProfilePicture() != null && user.getProfilePicture().length > 0 ? user.getProfilePicture() : null);
        return userResponseDTO;
        
        
    }

    public static User toModel(UserResponseDTO userResponseDTO) {
        if(userResponseDTO == null) {return null;}

        User user = new User();
        user.setId(userResponseDTO.getId() != null ? java.util.UUID.fromString(userResponseDTO.getId()) : null);
        user.setFirstName(userResponseDTO.getFirstName());
        user.setLastName(userResponseDTO.getLastName());
        user.setEmail(userResponseDTO.getEmail());
        user.setProfilePicture(userResponseDTO.getProfilePicture());
        return user;
    }
}
