// java
package com.laundry.app.controller;

import com.laundry.app.dto.UserCreateRequest;
import com.laundry.app.dto.UserMapper;
import com.laundry.app.dto.UserResponse;
import com.laundry.app.model.User;
import com.laundry.app.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for user management endpoints (create and list users, fetch by id).
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Construct a UserController with required dependencies.
     *
     * @param userService service to manage users
     * @param userMapper mapper to convert between DTOs and entities
     */
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Create a new user from a create request DTO.
     *
     * @param request DTO containing information to create a user
     * @return created user's response DTO
     */
    @PostMapping
    public UserResponse createUser(@RequestBody UserCreateRequest request) {
        User userEntity = userMapper.toEntity(request);
        User createdUser = userService.createUser(userEntity);
        return userMapper.toResponse(createdUser);
    }

    /**
     * Retrieve all users as response DTOs.
     *
     * @return list of UserResponse DTOs
     */
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a user by id.
     *
     * @param id user id
     * @return ResponseEntity with UserResponse or 404 if not found
     */
    @GetMapping("/{id}")
    public org.springframework.http.ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUser(id);
        if (user == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        return org.springframework.http.ResponseEntity.ok(userMapper.toResponse(user));
    }
}
