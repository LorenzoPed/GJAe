package com.laundry.app.view;

import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
import com.laundry.app.service.MachineService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@ViewScoped
public class MachineView implements Serializable {

    private final MachineService machineService;

    private List<Machine> machines;

    // Edit dialog state
    private Long selectedMachineId;
    private String editName;
    private MachineType editType;

    public MachineView(MachineService machineService) {
        this.machineService = machineService;
    }

    @PostConstruct
    public void init() {
        reloadMachines();
    }

    public void reloadMachines() {
        this.machines = machineService.getAllMachines();
    }

    /**
     * Prepares the edit dialog with the selected machine values.
     */
    public void openEdit(Machine machine) {
        if (machine == null) {
            return;
        }
        this.selectedMachineId = machine.getId();
        this.editName = machine.getName();
        this.editType = machine.getType();
    }

    /**
     * Saves the edit form (name + type). Does not change the machine enabled flag.
     */
    public void saveEdit() {
        FacesContext faces = FacesContext.getCurrentInstance();

        if (selectedMachineId == null) {
            faces.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error",
                "No machine selected."
            ));
            return;
        }

        try {
            machineService.updateMachineNameAndType(selectedMachineId, editName, editType);
            reloadMachines();

            faces.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Saved",
                "Machine updated successfully."
            ));
        } catch (IllegalArgumentException ex) {
            faces.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_WARN,
                "Validation",
                ex.getMessage()
            ));
        } catch (RuntimeException ex) {
            faces.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error",
                ex.getMessage()
            ));
        }
    }

    // --- Getters / Setters used by JSF ---

    public List<Machine> getMachines() {
        return machines;
    }

    public Long getSelectedMachineId() {
        return selectedMachineId;
    }

    public void setSelectedMachineId(Long selectedMachineId) {
        this.selectedMachineId = selectedMachineId;
    }

    public String getEditName() {
        return editName;
    }

    public void setEditName(String editName) {
        this.editName = editName;
    }

    public MachineType getEditType() {
        return editType;
    }

    public void setEditType(MachineType editType) {
        this.editType = editType;
    }

    public MachineType[] getMachineTypes() {
        return MachineType.values();
    }
}
