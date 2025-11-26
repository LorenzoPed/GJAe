package com.laundry.app.controller;

import com.laundry.app.model.User;
import com.laundry.app.service.LaundryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserController {

    private final LaundryService service;

    public UserController(LaundryService service) {
        this.service = service;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        return service.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) {
        User user = service.getUser(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        service.addUser(user);
        return user;
    }

    // petite exception custom pour 404
    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String id) {
            super("User not found: " + id);
        }
    }
}
