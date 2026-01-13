package com.laundry.app.service;

import com.laundry.app.model.User;
import com.laundry.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service handling user creation and basic lookups.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Construct the user service with repository and password encoder.
     *
     * @param userRepository user repository
     * @param passwordEncoder password encoder
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user: validate uniqueness, encode password and assign default role if missing.
     *
     * @param user user entity to create
     * @return persisted user
     */
    public User createUser(User user) {
        // Vérifie l'unicité (optionnel mais recommandé)
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        // Encode le mot de passe
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Définit un rôle par défaut si besoin (ex: USER)
        if (user.getRole() == null) {
            // Remplace par ton enum ou valeur par défaut
            user.setRole(com.laundry.app.model.Role.USER);
        }
        return userRepository.save(user);
    }

    /**
     * Retrieve all users.
     *
     * @return list of users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieve a user by id or return null if not found.
     *
     * @param id user id
     * @return user or null
     */
    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}