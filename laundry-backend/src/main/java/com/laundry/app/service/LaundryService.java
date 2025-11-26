package com.laundry.app.service;

import com.laundry.app.dto.Status;
import com.laundry.app.model.Machine;
import com.laundry.app.model.User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LaundryService {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Machine> machines = new ConcurrentHashMap<>();

    public LaundryService() {
        // Dummy initial data
        users.put("u1", new User("u1", "Alice", "alice@example.com"));
        users.put("u2", new User("u2", "Bob", "bob@example.com"));

        machines.put("m1", new Machine("m1", "washer", true));
        machines.put("m2", new Machine("m2", "dryer", false));
    }

    // -------- Status --------
    public Status getStatus() {
        return new Status("laundry-backend", "running");
    }

    // -------- Users --------
    public Collection<User> getAllUsers() {
        return users.values();
    }

    public User getUser(String id) {
        return users.get(id);
    }

    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    // -------- Machines --------
    public Collection<Machine> getAllMachines() {
        return machines.values();
    }

    public Machine getMachine(String id) {
        return machines.get(id);
    }

    public void updateMachineAvailability(String id, boolean available) {
        Machine machine = machines.get(id);
        if (machine != null) {
            machine.setAvailable(available);
        }
    }
}
