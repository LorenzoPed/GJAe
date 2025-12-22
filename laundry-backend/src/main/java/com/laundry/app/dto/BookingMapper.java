package com.laundry.app.dto;

import com.laundry.app.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }

        // Creiamo la risposta usando il costruttore che abbiamo fatto prima
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),            // Prendiamo solo l'ID dell'utente
                booking.getMachine().getName(),       // Prendiamo il nome della macchina (più utile dell'ID per l'utente)
                booking.getStartTime(),
                booking.getEndTime()
        );
    }
}