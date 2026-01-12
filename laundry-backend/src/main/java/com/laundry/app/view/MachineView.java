package com.laundry.app.view;

import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
import com.laundry.app.model.Maintenance;
import com.laundry.app.service.BookingService;
import com.laundry.app.service.MaintenanceService;
import com.laundry.app.service.MachineService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import org.primefaces.PrimeFaces;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ViewScoped
public class MachineView implements Serializable {

    /**
     * Target for edit dialog messages (must match ids in manager-dashboard.xhtml)
     */
    private static final String EDIT_DIALOG_CLIENT_ID = "editForm:editMachineDialog";

    private final MachineService machineService;
    private final MaintenanceService maintenanceService;
    private final BookingService bookingService;

    private List<Machine> machines;
    private List<Long> machineIdsUnderMaintenanceNow;

    private Long selectedMachineId;
    private String editName;
    private MachineType editType;

    private String newName;
    private MachineType newType;

    private Long maintenanceMachineId;
    private String maintenanceMachineName;
    private LocalDateTime maintenanceStart;
    private LocalDateTime maintenanceEnd;
    private String maintenanceReason;
    private List<Maintenance> upcomingMaintenances;

    public MachineView(
        MachineService machineService,
        MaintenanceService maintenanceService,
        BookingService bookingService
    ) {
        this.machineService = machineService;
        this.maintenanceService = maintenanceService;
        this.bookingService = bookingService;
    }

    @PostConstruct
    public void init() {
        reloadMachines();
    }

    public void reloadMachines() {
        this.machines = machineService.getAllMachines();
        this.machineIdsUnderMaintenanceNow = maintenanceService.getMachineIdsUnderMaintenanceNow();
    }

    public void openEdit(Machine machine) {
        if (machine == null) {
            return;
        }
        this.selectedMachineId = machine.getId();
        this.editName = machine.getName();
        this.editType = machine.getType();
    }

    public void saveEdit() {
        FacesContext faces = FacesContext.getCurrentInstance();
        setCallbackSuccess(false);

        if (selectedMachineId == null) {
            faces.addMessage(EDIT_DIALOG_CLIENT_ID, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error",
                "No machine selected."
            ));
            return;
        }

        try {
            machineService.updateMachineNameAndType(selectedMachineId, editName, editType);
            reloadMachines();

            // Success can remain global (background banner)
            faces.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Saved",
                "Machine updated successfully."
            ));

            setCallbackSuccess(true);
        } catch (IllegalArgumentException ex) {
            // Show inside the dialog
            faces.addMessage(EDIT_DIALOG_CLIENT_ID, new FacesMessage(
                FacesMessage.SEVERITY_WARN,
                "Validation",
                ex.getMessage()
            ));
        } catch (RuntimeException ex) {
            // Show inside the dialog
            faces.addMessage(EDIT_DIALOG_CLIENT_ID, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error",
                ex.getMessage()
            ));
        }
    }

    // This method is called from the Save button in the dialog
    public void createMachine() {
        try {
            // 1. Creiamo l'oggetto qui nel Controller
            Machine machine = new Machine();
            machine.setName(this.newName);
            machine.setType(this.newType);

            // 2. Impostiamo i valori di default (importante!)
            machine.setEnabled(true);

            // 3. Chiamiamo il TUO metodo esistente del service
            machineService.createMachine(machine);

            // 4. Aggiorniamo la lista visualizzata
            this.machines = machineService.getAllMachines();

            // 5. Messaggio di successo
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "New machine added !"));

            // Chiudiamo il dialog
            PrimeFaces.current().ajax().addCallbackParam("success", true);

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not save the machine."));
            PrimeFaces.current().ajax().addCallbackParam("success", false);
        }
    }

    public void openCreate() {
        this.newName = "";
        this.newType = MachineType.WASHER; // Default type
    }


    public void deleteMachine(Machine machine) {
        FacesContext faces = FacesContext.getCurrentInstance();

        if (machine == null) return;

        try {
            String resultMessage = machineService.deleteMachine(machine.getId());

            reloadMachines();

            faces.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Success",
                    resultMessage
            ));

        } catch (Exception e) {
            e.printStackTrace();
            faces.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "Could not delete machine: " + e.getMessage()
            ));
        }
    }

    public void toggleEnabled(Machine machine) {
        FacesContext faces = FacesContext.getCurrentInstance();
        setCallbackSuccess(false);

        if (machine == null || machine.getId() == null) {
            faces.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error",
                "No machine selected."
            ));
            return;
        }

        boolean newEnabled = !machine.isEnabled();

        try {
            Machine details = new Machine();
            details.setName(machine.getName());
            details.setType(machine.getType());
            details.setEnabled(newEnabled);

            machineService.updateMachine(machine.getId(), details);

            if (!newEnabled) {
                BookingService.DisableMachineResult result = bookingService.handleMachineDisabled(machine.getId());

                faces.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Machine disabled",
                    "Impacted bookings: " + result.getImpactedBookings()
                        + " (rescheduled: " + result.getRescheduledBookings()
                        + ", cancelled: " + result.getCancelledBookings() + ")"
                ));
            } else {
                faces.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Machine enabled",
                    "Machine status updated successfully."
                ));
            }

            reloadMachines();
            setCallbackSuccess(true);
        } catch (RuntimeException ex) {
            faces.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error",
                ex.getMessage()
            ));
        }
    }

    public void openMaintenance(Machine machine) {
        if (machine == null) {
            return;
        }

        this.maintenanceMachineId = machine.getId();
        this.maintenanceMachineName = machine.getName();
        this.maintenanceStart = null;
        this.maintenanceEnd = null;
        this.maintenanceReason = null;
        this.upcomingMaintenances = maintenanceService.getUpcomingMaintenances(machine.getId());
    }

    public void applyMaintenance() {
        FacesContext faces = FacesContext.getCurrentInstance();
        setCallbackSuccess(false);

        if (maintenanceMachineId == null) {
            faces.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error",
                "No machine selected."
            ));
            return;
        }

        try {
            MaintenanceService.MaintenanceResult result = maintenanceService.scheduleMaintenance(
                maintenanceMachineId,
                maintenanceStart,
                maintenanceEnd,
                maintenanceReason
            );

            reloadMachines();
            this.upcomingMaintenances = maintenanceService.getUpcomingMaintenances(maintenanceMachineId);

            faces.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Maintenance scheduled",
                "Impacted bookings: " + result.getImpactedBookings()
                    + " (rescheduled: " + result.getRescheduledBookings()
                    + ", cancelled: " + result.getCancelledBookings() + ")"
            ));

            setCallbackSuccess(true);
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

    public void cancelMaintenance(Long maintenanceId) {
        FacesContext faces = FacesContext.getCurrentInstance();
        setCallbackSuccess(false);

        try {
            maintenanceService.cancelMaintenance(maintenanceId);
            reloadMachines();

            if (maintenanceMachineId != null) {
                this.upcomingMaintenances = maintenanceService.getUpcomingMaintenances(maintenanceMachineId);
            }

            faces.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Cancelled",
                "Maintenance cancelled successfully."
            ));
            setCallbackSuccess(true);
        } catch (RuntimeException ex) {
            faces.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error",
                ex.getMessage()
            ));
        }
    }

    public boolean isUnderMaintenance(Machine machine) {
        if (machine == null || machineIdsUnderMaintenanceNow == null) {
            return false;
        }
        return machine.isEnabled() && machineIdsUnderMaintenanceNow.contains(machine.getId());
    }

    public String getStatusLabel(Machine machine) {
        if (machine == null) {
            return "";
        }
        if (!machine.isEnabled()) {
            return "Disabled";
        }
        if (isUnderMaintenance(machine)) {
            return "Maintenance";
        }
        return "Active";
    }

    public String getStatusSeverity(Machine machine) {
        if (machine == null) {
            return "info";
        }
        if (!machine.isEnabled()) {
            return "danger";
        }
        if (isUnderMaintenance(machine)) {
            return "warning";
        }
        return "success";
    }

    public String getStatusIcon(Machine machine) {
        if (machine == null) {
            return "pi pi-info-circle";
        }
        if (!machine.isEnabled()) {
            return "pi pi-times-circle";
        }
        if (isUnderMaintenance(machine)) {
            return "pi pi-wrench";
        }
        return "pi pi-check-circle";
    }

    private void setCallbackSuccess(boolean success) {
        PrimeFaces.current().ajax().addCallbackParam("success", success);
    }

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

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public MachineType getNewType() {
        return newType;
    }

    public void setNewType(MachineType newType) {
        this.newType = newType;
    }

    public Long getMaintenanceMachineId() {
        return maintenanceMachineId;
    }

    public void setMaintenanceMachineId(Long maintenanceMachineId) {
        this.maintenanceMachineId = maintenanceMachineId;
    }

    public String getMaintenanceMachineName() {
        return maintenanceMachineName;
    }

    public void setMaintenanceMachineName(String maintenanceMachineName) {
        this.maintenanceMachineName = maintenanceMachineName;
    }

    public LocalDateTime getMaintenanceStart() {
        return maintenanceStart;
    }

    public void setMaintenanceStart(LocalDateTime maintenanceStart) {
        this.maintenanceStart = maintenanceStart;
    }

    public LocalDateTime getMaintenanceEnd() {
        return maintenanceEnd;
    }

    public void setMaintenanceEnd(LocalDateTime maintenanceEnd) {
        this.maintenanceEnd = maintenanceEnd;
    }

    public String getMaintenanceReason() {
        return maintenanceReason;
    }

    public void setMaintenanceReason(String maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
    }

    public List<Maintenance> getUpcomingMaintenances() {
        return upcomingMaintenances;
    }
}
