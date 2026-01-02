package com.laundry.app.view;

import com.laundry.app.model.Machine;
import com.laundry.app.service.MachineService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import org.springframework.stereotype.Component;
import java.io.Serializable;
import java.util.List;

@Component
@ViewScoped
public class MachineView implements Serializable {

    private final MachineService machineService;
    private List<Machine> machines;

    public MachineView(MachineService machineService) {
        this.machineService = machineService;
    }

    @PostConstruct
    public void init() {
        this.machines = machineService.getAllMachines();
    }

    // --- AGGIUNGI QUESTO METODO MANUALMENTE ---
    public List<Machine> getMachines() {
        return machines;
    }
}
