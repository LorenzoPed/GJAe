package com.laundry.app.view;

import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.Booking;
import com.laundry.app.model.BookingStatus;
import com.laundry.app.model.MachineType;
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

    private ScheduleModel eventModel;

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

        List<Booking> activeBookings = bookingService.getActiveBookings();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        for (Booking b : activeBookings) {
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

            if (available && selectedType == null) {
                selectedType = type;
            }
            if (available) {
                atLeastOneAvailable = true;
            }
        }

        if (!atLeastOneAvailable) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "No machines available for this time slot.")
            );
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

            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Booking created!")
            );

            loadSchedule();
            loadMyBookings();
            loadAllBookings();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage())
            );
        }
    }

    public void loadMyBookings() {
        myBookings = bookingService.getMyActiveBookings();
    }

    public void loadAllBookings() {
        allBookings = bookingService.getAllBookings();
    }

    public void cancelBooking(Long id) {
        bookingService.cancelBooking(id);
        loadSchedule();
        loadMyBookings();
        loadAllBookings();

        FacesContext.getCurrentInstance().addMessage(
            null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Cancelled", "Booking cancelled")
        );
    }

    public boolean isCancelled(Booking booking) {
        return booking != null && booking.getStatus() == BookingStatus.CANCELLED;
    }

    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public LocalDate getClickedDate() {
        return clickedDate;
    }

    public void setClickedDate(LocalDate clickedDate) {
        this.clickedDate = clickedDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public MachineType getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(MachineType selectedType) {
        this.selectedType = selectedType;
    }

    public List<Booking> getMyBookings() {
        return myBookings;
    }

    public List<Booking> getAllBookings() {
        return allBookings;
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
