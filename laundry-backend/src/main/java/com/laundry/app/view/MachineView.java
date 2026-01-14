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

/**
 * JSF view bean for machine management: list/create/edit/delete and schedule maintenance.
 */
@Component
@ViewScoped
public class MachineView implements Serializable {

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

    /**
     * Construct the MachineView with required services.
     *
     * @param machineService service for machine operations
     * @param maintenanceService service for maintenance operations
     * @param bookingService service for booking operations (reschedule notifications)
     */
    public MachineView(
        MachineService machineService,
        MaintenanceService maintenanceService,
        BookingService bookingService
    ) {
        this.machineService = machineService;
        this.maintenanceService = maintenanceService;
        this.bookingService = bookingService;
    }

    /**
     * Initialize view and load machines.
     */
    @PostConstruct
    public void init() {
        reloadMachines();
    }

    /**
     * Reload machine list and maintenance state.
     */
    public void reloadMachines() {
        this.machines = machineService.getAllMachines();
        this.machineIdsUnderMaintenanceNow = maintenanceService.getMachineIdsUnderMaintenanceNow();
    }

    /**
     * Open the edit dialog and populate fields for the selected machine.
     *
     * @param machine machine to edit
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
     * Save edited machine name/type (validation handled in service).
     */
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

    /**
     * Create a new machine from dialog inputs.
     */
    // This method is called from the Save button in the dialog
    public void createMachine() {
        try {
            // 1. Create the object machine
            Machine machine = new Machine();
            machine.setName(this.newName);
            machine.setType(this.newType);

            // 2. Set default values
            machine.setEnabled(true);

            // 3. Call MachineService method
            machineService.createMachine(machine);

            // 4. Update the list shown in the dashboard
            this.machines = machineService.getAllMachines();

            // 5. Succes message
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "New machine added !"));

            // Close dialog
            PrimeFaces.current().ajax().addCallbackParam("success", true);

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not save the machine."));
            PrimeFaces.current().ajax().addCallbackParam("success", false);
        }
    }

    /**
     * Delete a machine and handle impacted bookings/maintenances.
     *
     * @param machine target machine
     */
    public void deleteMachine(Machine machine) {
        FacesContext faces = FacesContext.getCurrentInstance();

        if (machine == null) return;

        try {
            //Call MachineService method
            BookingService.DisableMachineResult result = machineService.deleteMachine(machine.getId());

            //Update the view
            reloadMachines();

            faces.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Machine deleted successfully",
                    "Impacted bookings: " + result.getImpactedBookings()
                        + " (rescheduled: " + result.getRescheduledBookings()
                        + ", cancelled: " + result.getCancelledBookings() + ")"
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

    /**
     * Toggle the enabled state of a machine and handle impact if disabling.
     *
     * @param machine machine to toggle
     */
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
        BookingService.DisableMachineResult result = null;
        
        // Si on désactive, traiter les bookings AVANT la mise à jour
        if (!newEnabled) {
            result = bookingService.handleMachineDisabled(machine.getId());
        }

        // Puis mettre à jour la machine
        Machine details = new Machine();
        details.setName(machine.getName());
        details.setType(machine.getType());
        details.setEnabled(newEnabled);

        machineService.updateMachine(machine.getId(), details);

        if (!newEnabled && result != null) {
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

    /**
     * Open maintenance dialog and load upcoming maintenances for the machine.
     *
     * @param machine target machine
     */
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

    /**
     * Apply maintenance window and notify impacted users.
     */
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

    /**
     * Cancel a maintenance record.
     *
     * @param maintenanceId id of the maintenance to cancel
     */
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

    /** Returns the list of machines for display. */
    public List<Machine> getMachines() {
        return machines;
    }

    /** Returns the selected machine id for edit dialogs. */
    public Long getSelectedMachineId() {
        return selectedMachineId;
    }

    /** Set selected machine id. */
    public void setSelectedMachineId(Long selectedMachineId) {
        this.selectedMachineId = selectedMachineId;
    }

    /** Returns the edit dialog name field. */
    public String getEditName() {
        return editName;
    }

    /** Set the edit dialog name field. */
    public void setEditName(String editName) {
        this.editName = editName;
    }

    /** Returns the edit dialog machine type. */
    public MachineType getEditType() {
        return editType;
    }

    /** Set the edit dialog machine type. */
    public void setEditType(MachineType editType) {
        this.editType = editType;
    }

    /** Returns available machine types for select components. */
    public MachineType[] getMachineTypes() {
        return MachineType.values();
    }

    /** Returns the new machine name input. */
    public String getNewName() {
        return newName;
    }

    /** Set the new machine name input. */
    public void setNewName(String newName) {
        this.newName = newName;
    }

    /** Returns the new machine type input. */
    public MachineType getNewType() {
        return newType;
    }

    /** Set the new machine type input. */
    public void setNewType(MachineType newType) {
        this.newType = newType;
    }

    /** Returns the id of the machine for which maintenance is being scheduled. */
    public Long getMaintenanceMachineId() {
        return maintenanceMachineId;
    }

    /** Set the id of the machine for which maintenance is being scheduled. */
    public void setMaintenanceMachineId(Long maintenanceMachineId) {
        this.maintenanceMachineId = maintenanceMachineId;
    }

    /** Returns maintenance machine display name. */
    public String getMaintenanceMachineName() {
        return maintenanceMachineName;
    }

    /** Set maintenance machine display name. */
    public void setMaintenanceMachineName(String maintenanceMachineName) {
        this.maintenanceMachineName = maintenanceMachineName;
    }

    /** Returns maintenance start datetime. */
    public LocalDateTime getMaintenanceStart() {
        return maintenanceStart;
    }

    /** Set maintenance start datetime. */
    public void setMaintenanceStart(LocalDateTime maintenanceStart) {
        this.maintenanceStart = maintenanceStart;
    }

    /** Returns maintenance end datetime. */
    public LocalDateTime getMaintenanceEnd() {
        return maintenanceEnd;
    }

    /** Set maintenance end datetime. */
    public void setMaintenanceEnd(LocalDateTime maintenanceEnd) {
        this.maintenanceEnd = maintenanceEnd;
    }

    /** Returns maintenance reason text. */
    public String getMaintenanceReason() {
        return maintenanceReason;
    }

    /** Set maintenance reason text. */
    public void setMaintenanceReason(String maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
    }

    /** Returns upcoming maintenances for the selected machine. */
    public List<Maintenance> getUpcomingMaintenances() {
        return upcomingMaintenances;
    }
}
