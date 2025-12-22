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

    // Ritorna tutte le macchine
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    // Trova una macchina per ID (lancia eccezione se non esiste)
    public Machine getMachineById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine not found with id: " + id));
    }

    // Crea una nuova macchina
    public Machine createMachine(Machine machine) {
        return machineRepository.save(machine);
    }

    // Aggiorna nome e stato (enabled)
    public Machine updateMachine(Long id, Machine machineDetails) {
        Machine machine = getMachineById(id); // Riutilizza il metodo sopra per il check esistenza

        machine.setName(machineDetails.getName());
        machine.setEnabled(machineDetails.isEnabled()); // Qui usiamo il tuo campo 'enabled'

        return machineRepository.save(machine);
    }

    // Cancella una macchina
    public void deleteMachine(Long id) {
        if (!machineRepository.existsById(id)) {
            throw new RuntimeException("Machine not found with id: " + id);
        }
        machineRepository.deleteById(id);
    }
}