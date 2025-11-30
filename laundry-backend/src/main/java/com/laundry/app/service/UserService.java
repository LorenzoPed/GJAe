
package com.laundry.app.service;

import com.laundry.app.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    public UserService() {
        // Dummy initial data moved from LaundryService
        users.put("u1", new User("u1", "Alice", "alice@example.com"));
        users.put("u2", new User("u2", "Bob", "bob@example.com"));
    }

    public User createUser(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        users.put(user.getId(), user);
        return user;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public User getUser(String id) {
        return users.get(id);
    }
}
