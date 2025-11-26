package com.laundry.app.controller;

import com.laundry.app.model.Machine;
import com.laundry.app.service.LaundryService;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/machines")
public class MachineController {

    private final LaundryService service;

    public MachineController(LaundryService service) {
        this.service = service;
    }

    @GetMapping
    public Collection<Machine> getAllMachines() {
        return service.getAllMachines();
    }

    @GetMapping("/{id}")
    public Machine getMachine(@PathVariable String id) {
        Machine machine = service.getMachine(id);
        if (machine == null) {
            throw new MachineNotFoundException(id);
        }
        return machine;
    }

    @PatchMapping("/{id}/availability")
    public Machine updateAvailability(@PathVariable String id,
                                      @RequestParam boolean available) {
        service.updateMachineAvailability(id, available);
        Machine machine = service.getMachine(id);
        if (machine == null) {
            throw new MachineNotFoundException(id);
        }
        return machine;
    }

    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    static class MachineNotFoundException extends RuntimeException {
        public MachineNotFoundException(String id) {
            super("Machine not found: " + id);
        }
    }
}
