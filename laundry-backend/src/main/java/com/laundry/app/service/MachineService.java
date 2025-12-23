package com.laundry.app.service;

import com.laundry.app.model.Machine;
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

    //Returns all machines
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

    // Update name and status (enabled)
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