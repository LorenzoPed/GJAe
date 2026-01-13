// java
package com.laundry.app.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;
import com.laundry.app.service.UserService;
import com.laundry.app.model.User;

/**
 * Backing bean handling user registration from the web UI.
 */
@Data
@Named
@RequestScoped
public class RegisterController {
    private String username;
    private String password;
    private String email;

    @Inject
    private UserService userService;

    /**
     * Register a new user with provided username, password and email.
     *
     * @return navigation outcome to the login page with faces redirect
     */
    public String register() {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        userService.createUser(user);
        return "login?faces-redirect=true";
    }
}
