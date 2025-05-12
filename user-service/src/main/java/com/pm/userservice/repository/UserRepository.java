package com.pm.userservice.repository;

import com.pm.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {


    // Find a user by their email address
    Optional<User> findByEmail(String email);

    // Check if a user exists with the given email
    boolean existsByEmail(String email);

    // Check if any user other than the one with given ID has this email
    boolean existsByEmailAndIdNot(String email, UUID id);

}
