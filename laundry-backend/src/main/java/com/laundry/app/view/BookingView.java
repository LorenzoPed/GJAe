package com.laundry.app.view;

import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.Booking;
import com.laundry.app.model.MachineType;
import com.laundry.app.service.BookingService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList; // Importante per la lista dinamica
import java.util.List;

@Named
@ViewScoped
public class BookingView implements Serializable {

    @Autowired
    private BookingService bookingService;

    // --- Calendar Data ---
    private ScheduleModel eventModel;

    // --- Selection Data ---
    private LocalDate clickedDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private MachineType selectedType;

    // --- Tables Data ---
    private List<Booking> myBookings;
    private List<Booking> allBookings;
    private List<Booking> filteredBookings;

    // --- CHANGE 1: Use SelectItem instead of MachineType ---
    // This list allows us to control the label and the disabled state per item
    private List<SelectItem> machineTypeOptions;

    @PostConstruct
    public void init() {
        eventModel = new DefaultScheduleModel();
        loadSchedule();
        loadMyBookings();
        loadAllBookings();
    }

    /**
     * Carica gli eventi nel calendario.
     */
    public void loadSchedule() {
        eventModel.clear();
        List<Booking> allBookings = bookingService.getAllBookings();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        for (Booking b : allBookings) {
            boolean isMine = b.getUser().getUsername().equals(currentUsername);

            DefaultScheduleEvent<?> event = DefaultScheduleEvent.builder()
                    .title(isMine ? "My " + b.getMachine().getType() : "Reserved")
                    .startDate(b.getStartTime())
                    .endDate(b.getEndTime())
                    .description("Machine: " + b.getMachine().getName())
                    .borderColor(isMine ? "#28a745" : "#6c757d")
                    .backgroundColor(isMine ? "#28a745" : "#e9ecef")
                    .textColor(isMine ? "#ffffff" : "#495057")
                    .build();

            eventModel.addEvent(event);
        }
    }

    // ... imports invariati ...

    // Metodo helper privato per ricalcolare le opzioni
    private void updateAvailability() {
        LocalDateTime startDateTime = LocalDateTime.of(clickedDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(clickedDate, endTime);

        machineTypeOptions = new ArrayList<>();
        boolean atLeastOneAvailable = false;
        this.selectedType = null;

        for (MachineType type : MachineType.values()) {
            boolean isAvailable = bookingService.isSlotAvailable(type, startDateTime, endDateTime);
            String label = type.toString();
            if (!isAvailable) {
                label += " (Not Available)";
            }

            // Aggiungi alla lista (disabilitato se non disponibile)
            machineTypeOptions.add(new SelectItem(type, label, null, !isAvailable));

            if (isAvailable) {
                atLeastOneAvailable = true;
                if (this.selectedType == null) {
                    this.selectedType = type;
                }
            }
        }

        // Se tutto è pieno, avvisa l'utente ma NON bloccare il dialog (lo vede visivamente)
        if (!atLeastOneAvailable) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "No machines available for this time slot."));
        }
    }

    // Viene chiamato al click sul calendario
    public void onDateSelect(SelectEvent<LocalDateTime> selectEvent) {
        this.clickedDate = selectEvent.getObject().toLocalDate();
        this.startTime = selectEvent.getObject().toLocalTime();
        this.endTime = this.startTime.plusHours(1);

        // Calcola disponibilità iniziale e APRI IL DIALOG COMUNQUE
        updateAvailability();
    }

    // NUOVO: Viene chiamato quando l'utente cambia l'orario nel dialog
    public void onTimeChange() {
        // Se l'utente inverte gli orari per sbaglio, gestiamo l'errore o ricalcoliamo
        if (startTime != null && endTime != null) {
            updateAvailability();
        }
    }

    /**
     * Conferma la prenotazione.
     */
    public void createBooking() {
        try {
            LocalDateTime startDateTime = LocalDateTime.of(clickedDate, startTime);
            LocalDateTime endDateTime = LocalDateTime.of(clickedDate, endTime);

            BookingRequest request = new BookingRequest();
            request.setStartTime(startDateTime);
            request.setEndTime(endDateTime);
            request.setMachineType(selectedType);

            bookingService.createBooking(request);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Booking created!"));

            loadSchedule();
            loadMyBookings();
            loadAllBookings(); // Aggiorna anche per il manager

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void loadMyBookings() {
        myBookings = bookingService.getMyBookings();
    }

    public void loadAllBookings() {
        this.allBookings = bookingService.getAllBookings();
    }

    public void cancelBooking(Long id) {
        bookingService.cancelBooking(id);
        loadSchedule();
        loadMyBookings();
        loadAllBookings();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cancelled", "Booking removed"));
    }

    // --- Getters & Setters ---
    public ScheduleModel getEventModel() { return eventModel; }

    public LocalDate getClickedDate() { return clickedDate; }
    public void setClickedDate(LocalDate clickedDate) { this.clickedDate = clickedDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public MachineType getSelectedType() { return selectedType; }
    public void setSelectedType(MachineType selectedType) { this.selectedType = selectedType; }

    public MachineType[] getMachineTypes() { return MachineType.values(); }
    public List<Booking> getMyBookings() { return myBookings; }

    public List<Booking> getAllBookings() {
        return this.allBookings;
    }

    public void setAllBookings(List<Booking> allBookings) {
        this.allBookings = allBookings;
    }

    public List<Booking> getFilteredBookings() {
        return filteredBookings;
    }

    public void setFilteredBookings(List<Booking> filteredBookings) {
        this.filteredBookings = filteredBookings;
    }

    public List<SelectItem> getMachineTypeOptions() {
        return machineTypeOptions;
    }
}