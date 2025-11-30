package com.laundry.app.service;

import com.laundry.app.model.Machine;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MachineService {

    private final Map<String, Machine> machines = new ConcurrentHashMap<>();

    public MachineService() {
        // Dummy initial data moved from LaundryService
        machines.put("m1", new Machine("m1", "washer", true));
        machines.put("m2", new Machine("m2", "dryer", false));
    }

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