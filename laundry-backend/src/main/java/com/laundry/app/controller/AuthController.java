package com.laundry.app.controller;

import com.laundry.app.dto.AuthResponse;
import com.laundry.app.dto.LoginRequest;
import com.laundry.app.dto.RegisterRequest;
import com.laundry.app.model.Role;
import com.laundry.app.model.User;
import com.laundry.app.repository.UserRepository;
import com.laundry.app.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils,
                          AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    // 1. REGISTER ENDPOINT
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        // Create new user object
        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // ENCRYPT PASSWORD before saving!
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Set default role (USER)
        user.setRole(Role.USER);

        // Save to DB
        userRepository.save(user);

        // Generate token immediately so user is logged in
        String token = jwtUtils.generateToken(user.getUsername());

        return ResponseEntity.ok(new AuthResponse(token));
    }

    // 2. LOGIN ENDPOINT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // This validates the username and password automatically using Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // If authentication passed, generate the token
        String token = jwtUtils.generateToken(request.getUsername());

        return ResponseEntity.ok(new AuthResponse(token));
    }
}