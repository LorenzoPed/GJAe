package com.laundry.app.controller;

import com.laundry.app.model.Machine;
import com.laundry.app.service.MachineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService machineService;

    // Constructor Injection
    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    // 1. Get all machines
    @GetMapping
    public List<Machine> getAllMachines() {
        return machineService.getAllMachines();
    }

    // 2. Get machine by ID
    @GetMapping("/{id}")
    public ResponseEntity<Machine> getMachineById(@PathVariable Long id) {
        // FIXED: Now calling 'getMachineById' instead of 'getMachine'
        return ResponseEntity.ok(machineService.getMachineById(id));
    }

    // 3. Create machine
    @PostMapping
    public ResponseEntity<Machine> createMachine(@RequestBody Machine machine) {
        return ResponseEntity.ok(machineService.createMachine(machine));
    }

    // 4. Update machine (Replaces the old updateMachineAvailability)
    // Send the full object (e.g., {"name": "...", "enabled": false})
    @PutMapping("/{id}")
    public ResponseEntity<Machine> updateMachine(@PathVariable Long id, @RequestBody Machine machineDetails) {
        // FIXED: Now calling 'updateMachine' which handles both name and enabled status
        return ResponseEntity.ok(machineService.updateMachine(id, machineDetails));
    }

    // 5. Delete machine
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMachine(@PathVariable Long id) {
        machineService.deleteMachine(id);
        return ResponseEntity.noContent().build();
    }
}