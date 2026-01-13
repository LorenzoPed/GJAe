package com.laundry.app.repository;
import java.util.Optional;

import com.laundry.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for User entities and common existence checks used at registration/login.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Find a user by username for authentication.
     * @param username the username
     * @return optional user
     */
    Optional<User> findByUsername(String username);

    /**
     * Check whether a username already exists.
     * @param username username to check
     * @return true if exists
     */
    boolean existsByUsername(String username);

    /**
     * Check whether an email is already registered.
     * @param email email to check
     * @return true if exists
     */
    boolean existsByEmail(String email);
}