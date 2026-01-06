package com.laundry.app.service;

import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
import com.laundry.app.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MachineService {

    private final MachineRepository machineRepository;

    @Autowired
    public MachineService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    // Returns all machines
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    // Find machine by ID
    public Machine getMachineById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine not found with id: " + id));
    }

    // Create new machine
    public Machine createMachine(Machine machine) {
        return machineRepository.save(machine);
    }

    /**
     * Updates only the machine name and type (does not change enabled flag).
     */
    public Machine updateMachineNameAndType(Long id, String name, MachineType type) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Machine name cannot be empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Machine type is required.");
        }

        Machine machine = getMachineById(id);
        machine.setName(name.trim());
        machine.setType(type);

        return machineRepository.save(machine);
    }

    // Update machine (full update used by REST controller)
    public Machine updateMachine(Long id, Machine machineDetails) {
        Machine machine = getMachineById(id);

        machine.setName(machineDetails.getName());
        machine.setEnabled(machineDetails.isEnabled());
        machine.setType(machineDetails.getType());

        return machineRepository.save(machine);
    }

    // Delete a Machine
    public void deleteMachine(Long id) {
        if (!machineRepository.existsById(id)) {
            throw new RuntimeException("Machine not found with id: " + id);
        }
        machineRepository.deleteById(id);
    }
}
