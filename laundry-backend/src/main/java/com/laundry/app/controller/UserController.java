package com.laundry.app.controller;

import com.laundry.app.dto.UserCreateRequest;
import com.laundry.app.dto.UserMapper;
import com.laundry.app.dto.UserResponse;
import com.laundry.app.model.User;
import com.laundry.app.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper; // Inject the mapper

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping
    public UserResponse createUser(@RequestBody UserCreateRequest request) {
        // Convert DTO -> Entity
        User userEntity = userMapper.toEntity(request);

        // Call Service
        User createdUser = userService.createUser(userEntity);

        // Convert Entity -> DTO
        return userMapper.toResponse(createdUser);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public org.springframework.http.ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUser(id);
        if (user == null) {
            return org.springframework.http.ResponseEntity.notFound().build(); // Restituisce 404 se non esiste
        }
        return org.springframework.http.ResponseEntity.ok(userMapper.toResponse(user)); // Restituisce 200 OK con l'utente
    }
}