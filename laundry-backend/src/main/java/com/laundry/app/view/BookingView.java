package com.laundry.app.view;

import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.Booking;
import com.laundry.app.service.BookingService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Named
@SessionScoped
public class BookingView implements Serializable {

    @Inject
    private BookingService bookingService;

    // Form fields (bind to XHTML inputs)
    private Date startTime;
    private Date endTime;

    // Data for tables
    private List<Booking> myBookings;
    private List<Booking> allBookings; // Only for Manager
    private List<Booking> filteredBookings;

    @PostConstruct
    public void init() {
        loadMyBookings();

        // Load all bookings only if user is MANAGER
        if (isManager()) {
            loadAllBookings();
        }
    }

    // ========== CREATE BOOKING ==========
    public void createBooking() {
        try {
            // Convert Date to LocalDateTime
            LocalDateTime start = convertToLocalDateTime(startTime);
            LocalDateTime end = convertToLocalDateTime(endTime);

            // Create DTO request
            BookingRequest request = new BookingRequest();
            request.setStartTime(start);
            request.setEndTime(end);

            // Call service (auto-assigns machine)
            bookingService.createBooking(request);

            // Success message
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Booking created successfully!"));

            // Reload bookings and clear form
            loadMyBookings();
            if (isManager()) {
                loadAllBookings();
            }
            clearForm();

        } catch (IllegalArgumentException e) {
            // Validation errors (bad dates, past time, etc.)
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        } catch (IllegalStateException e) {
            // No machines available
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", e.getMessage()));
        }
    }

    // ========== CANCEL BOOKING ==========
    public void cancelBooking(Long bookingId) {
        try {
            bookingService.cancelBooking(bookingId);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Booking cancelled successfully"));

            // Reload lists
            loadMyBookings();
            if (isManager()) {
                loadAllBookings();
            }

        } catch (RuntimeException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    // ========== LOAD DATA ==========
    public void loadMyBookings() {
        myBookings = bookingService.getMyBookings();
    }

    public void loadAllBookings() {
        allBookings = bookingService.getAllBookings();
    }

    // ========== UTILITY METHODS ==========
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private void clearForm() {
        startTime = null;
        endTime = null;
    }

    public boolean isManager() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
    }

    public List<Booking> getFilteredBookings() {
        return filteredBookings;
    }

    public void setFilteredBookings(List<Booking> filteredBookings) {
        this.filteredBookings = filteredBookings;
    }

    public String calculateDuration(Booking booking) {
        long hours = java.time.Duration.between(booking.getStartTime(), booking.getEndTime()).toHours();
        return hours + " hours";
    }

    // ========== GETTERS & SETTERS ==========
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public List<Booking> getMyBookings() { return myBookings; }
    public void setMyBookings(List<Booking> myBookings) { this.myBookings = myBookings; }

    public List<Booking> getAllBookings() { return allBookings; }
    public void setAllBookings(List<Booking> allBookings) { this.allBookings = allBookings; }
}
