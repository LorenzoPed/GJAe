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

    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    public Machine getMachineById(Long id) {
        return machineRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Machine not found with id: " + id));
    }

    public Machine createMachine(Machine machine) {
        return machineRepository.save(machine);
    }

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

    public Machine updateMachine(Long id, Machine machineDetails) {
        Machine machine = getMachineById(id);

        machine.setName(machineDetails.getName());
        machine.setEnabled(machineDetails.isEnabled());
        machine.setType(machineDetails.getType());

        return machineRepository.save(machine);
    }

    public void deleteMachine(Long id) {
        if (!machineRepository.existsById(id)) {
            throw new RuntimeException("Machine not found with id: " + id);
        }
        machineRepository.deleteById(id);
    }
}
