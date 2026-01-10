package com.laundry.app.view;

import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.Booking;
import com.laundry.app.model.BookingStatus;
import com.laundry.app.model.MachineType;
import com.laundry.app.model.User;
import com.laundry.app.repository.UserRepository;
import com.laundry.app.service.BookingService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class BookingView implements Serializable {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    private ScheduleModel eventModel;

    // Inizializziamo l'evento per evitare NullPointerException
    private ScheduleEvent<?> event = new DefaultScheduleEvent<>();

    private LocalDate clickedDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private MachineType selectedType;

    private List<Booking> myBookings;
    private List<Booking> allBookings;
    private List<Booking> filteredBookings;

    private List<SelectItem> machineTypeOptions;

    @PostConstruct
    public void init() {
        eventModel = new DefaultScheduleModel();
        loadSchedule();
        loadMyBookings();
        loadAllBookings();
    }

    public void loadSchedule() {
        eventModel.clear();

        boolean isManager = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        List<Booking> bookingsToShow;
        if (isManager) {
            bookingsToShow = bookingService.getAllBookings();
        } else {
            bookingsToShow = bookingService.getActiveBookings();
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        for (Booking b : bookingsToShow) {

            // --- MODIFICA QUI: Nascondi SEMPRE i cancellati, anche al Manager ---
            if (b.getStatus() == BookingStatus.CANCELLED) {
                continue;
            }
            // --------------------------------------------------------------------

            String title;
            String color;
            String textColor = "#ffffff";

            if (isManager) {
                title = b.getUser().getUsername() + " (" + b.getMachine().getType() + ")";

                if (b.getStatus() == BookingStatus.COMPLETED) {
                    color = "#bdbdbd"; // Grigio per i passati
                } else {
                    color = "#1976D2"; // Blu standard per attivi
                }
            } else {
                boolean isMine = b.getUser().getUsername().equals(currentUsername);
                title = isMine ? "My " + b.getMachine().getType() : "Reserved";
                color = isMine ? "#28a745" : "#6c757d";
            }

            DefaultScheduleEvent<?> eventItem = DefaultScheduleEvent.builder()
                    .title(title)
                    .startDate(b.getStartTime())
                    .endDate(b.getEndTime())
                    .description("User: " + b.getUser().getUsername() + "\nMachine: " + (b.getMachine() != null ? b.getMachine().getName() : "Deleted"))
                    .borderColor(color)
                    .backgroundColor(color)
                    .textColor(textColor)
                    .data(b.getId())
                    .build();

            eventModel.addEvent(eventItem);
        }
    }

    public void onEventSelect(SelectEvent<ScheduleEvent<?>> selectEvent) {
        this.event = selectEvent.getObject();
    }

    public void deleteSelectedEvent() {
        if (event != null && event.getData() != null) {
            Long bookingId = (Long) event.getData();
            try {
                bookingService.cancelBooking(bookingId);

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Booking cancelled."));

                loadSchedule();
                loadAllBookings();
                loadMyBookings();
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No booking selected."));
        }
    }

    // --- Metodi invariati ---
    private void updateAvailability() {
        LocalDateTime startDateTime = LocalDateTime.of(clickedDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(clickedDate, endTime);

        machineTypeOptions = new ArrayList<>();
        boolean atLeastOneAvailable = false;
        selectedType = null;

        for (MachineType type : MachineType.values()) {
            boolean available = bookingService.isSlotAvailable(type, startDateTime, endDateTime);
            String label = available ? type.toString() : type + " (Not Available)";
            machineTypeOptions.add(new SelectItem(type, label, null, !available));

            if (available && selectedType == null) selectedType = type;
            if (available) atLeastOneAvailable = true;
        }

        if (!atLeastOneAvailable) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "No machines available for this time slot."));
        }
    }

    public void onDateSelect(SelectEvent<LocalDateTime> selectEvent) {
        LocalDateTime selected = selectEvent.getObject();
        clickedDate = selected.toLocalDate();
        startTime = selected.toLocalTime();
        endTime = startTime.plusHours(1);
        updateAvailability();
    }

    public void onTimeChange() {
        if (clickedDate != null && startTime != null && endTime != null) {
            updateAvailability();
        }
    }

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
            loadAllBookings();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void loadMyBookings() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        if (currentUser != null) {
            myBookings = bookingService.getAllBookingsByUser(currentUser);
        } else {
            myBookings = new ArrayList<>();
        }
    }

    public void loadAllBookings() { allBookings = bookingService.getAllBookings(); }

    public void cancelBooking(Long id) {
        bookingService.cancelBooking(id);
        loadSchedule();
        loadMyBookings();
        loadAllBookings();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cancelled", "Booking cancelled"));
    }

    public void cancelAllBookingsForUser(User user) {
        if (user == null) return;
        try {
            bookingService.cancelAllUserBookings(user.getId());
            this.allBookings = bookingService.getAllBookings();
            loadSchedule();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "All bookings cancelled."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not cancel bookings."));
        }
    }

    // Getters & Setters
    public ScheduleModel getEventModel() { return eventModel; }
    public ScheduleEvent<?> getEvent() { return event; }
    public void setEvent(ScheduleEvent<?> event) { this.event = event; }
    public LocalDate getClickedDate() { return clickedDate; }
    public void setClickedDate(LocalDate clickedDate) { this.clickedDate = clickedDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public MachineType getSelectedType() { return selectedType; }
    public void setSelectedType(MachineType selectedType) { this.selectedType = selectedType; }
    public List<Booking> getMyBookings() { return myBookings; }
    public List<Booking> getAllBookings() { return allBookings; }
    public void setAllBookings(List<Booking> allBookings) { this.allBookings = allBookings; }
    public List<SelectItem> getMachineTypeOptions() { return machineTypeOptions; }
    public List<Booking> getFilteredBookings() { return filteredBookings; }
    public void setFilteredBookings(List<Booking> filteredBookings) { this.filteredBookings = filteredBookings; }
}