package com.laundry.app.controller;

import com.laundry.app.model.Machine;
import com.laundry.app.service.MachineService;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/machines")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @GetMapping
    public Collection<Machine> getAllMachines() {
        return machineService.getAllMachines();
    }

    @GetMapping("/{id}")
    public Machine getMachine(@PathVariable String id) {
        return machineService.getMachine(id);
    }

    @PutMapping("/{id}/availability")
    public void updateAvailability(@PathVariable String id, @RequestParam boolean available) {
        machineService.updateMachineAvailability(id, available);
    }
}
