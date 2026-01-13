// java
package com.laundry.app.controller;

import com.laundry.app.model.Machine;
import com.laundry.app.service.MachineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing machine management endpoints.
 */
@RestController
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService machineService;

    /**
     * Construct a MachineController with the provided MachineService.
     *
     * @param machineService service used to manage machines
     */
    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    /**
     * Retrieve all machines.
     *
     * @return list of all machines
     */
    @GetMapping
    public List<Machine> getAllMachines() {
        return machineService.getAllMachines();
    }

    /**
     * Retrieve a machine by its id.
     *
     * @param id machine id
     * @return ResponseEntity with machine and HTTP 200, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Machine> getMachineById(@PathVariable Long id) {
        Machine machine = machineService.getMachineById(id);
        if (machine == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(machine);
    }

    /**
     * Create a new machine.
     *
     * @param machine machine payload to create
     * @return created machine wrapped in ResponseEntity
     */
    @PostMapping
    public ResponseEntity<Machine> createMachine(@RequestBody Machine machine) {
        return ResponseEntity.ok(machineService.createMachine(machine));
    }

    /**
     * Update an existing machine with provided details.
     *
     * @param id id of the machine to update
     * @param machineDetails updated machine fields
     * @return updated Machine wrapped in ResponseEntity
     */
    @PutMapping("/{id}")
    public ResponseEntity<Machine> updateMachine(@PathVariable Long id, @RequestBody Machine machineDetails) {
        return ResponseEntity.ok(machineService.updateMachine(id, machineDetails));
    }

    /**
     * Delete a machine by id.
     *
     * @param id id of the machine to delete
     * @return HTTP 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMachine(@PathVariable Long id) {
        machineService.deleteMachine(id);
        return ResponseEntity.noContent().build();
    }
}
