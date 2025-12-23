package com.laundry.app.repository;
import java.util.Optional;

import com.laundry.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 1. FIND USER FOR LOGIN (Essential)
    // Spring Security uses this to load details of the user attempting to log in
    Optional<User> findByUsername(String username);

    // 2. EXISTENCE CHECKS (Useful for Registration)
    // Used to prevent duplicate usernames or emails
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}