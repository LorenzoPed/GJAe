package com.laundry.app.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;
import com.laundry.app.service.UserService;
import com.laundry.app.model.User;

@Data
@Named
@RequestScoped
public class RegisterController {
    private String username;
    private String password;
    private String email;

    @Inject
    private UserService userService;

    public String register() {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        userService.createUser(user);
        return "login?faces-redirect=true";
    }
}