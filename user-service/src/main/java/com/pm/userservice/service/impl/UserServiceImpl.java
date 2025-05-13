package com.pm.userservice.service.impl;

import com.pm.userservice.dto.UserRequestDTO;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.exception.EmailAlreadyExistsException;
import com.pm.userservice.exception.UserNotFoundException;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.model.User;
import com.pm.userservice.repository.UserRepository;
import com.pm.userservice.service.UserService;
import com.pm.userservice.util.PasswordEncoderHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoderHelper passwordEncoder;

    // Constructor injection of dependencies
    public UserServiceImpl(UserRepository userRepository, PasswordEncoderHelper passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Retrieve all users and convert them to DTOs
    @Override
    public List<UserResponseDTO> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }

    // Create a new user
    @Override
    public UserResponseDTO createUser(UserRequestDTO dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("A user with this email already exists: " + dto.getEmail());
        }

        // Convert DTO to user entity and set additional fields
        User user = UserMapper.toModel(dto);
        user.setPassword(passwordEncoder.hash(dto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Save user and return response DTO
        return UserMapper.toDTO(userRepository.save(user));
    }

    // Update an existing user
    @Override
    public UserResponseDTO updateUser(UUID id, UserRequestDTO dto) {
        // Find existing user or throw exception if not found
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check if new email conflicts with another user
        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new EmailAlreadyExistsException("A user with this email already exists.");
        }

        // Update user fields
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setProfilePictureUrl(dto.getProfilePictureUrl());
        user.setUpdatedAt(LocalDateTime.now());

        // Save updated user and return response DTO
        return UserMapper.toDTO(userRepository.save(user));
    }

    // Delete a user by ID
    @Override
    public void deleteUser(UUID id) {
        // Check if user exists before deletion
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
}