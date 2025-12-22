
package com.laundry.app.service;

import com.laundry.app.model.User;
import com.laundry.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    // Instead of Map we use the repository
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        // save() saves in the DB and returns the user with new ID
        return userRepository.save(user);
    }

    // Read all users from database
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Read one specific user
    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}