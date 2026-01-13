// java
package com.laundry.app.dto;

import com.laundry.app.model.Booking;
import org.springframework.stereotype.Component;

/**
 * Mapper component to convert Booking entities to DTOs used by the API.
 */
@Component
public class BookingMapper {

    /**
     * Convert a Booking entity into a BookingResponse DTO.
     *
     * @param booking booking entity to convert, may be null
     * @return BookingResponse DTO or null if booking is null
     */
    public BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }

        // Creiamo la risposta usando il costruttore che abbiamo fatto prima
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getMachine().getName(),
                booking.getStartTime(),
                booking.getEndTime()
        );
    }
}
