package com.laundry.app.repository;

import com.laundry.app.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Custom query to find all bookings belonging to a specific user
    List<Booking> findByUserId(Long userId);

    // Custom query to find all bookings for a specific machine
    List<Booking> findByMachineId(Long machineId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.machine.id = :machineId " +
            "AND ((:start < b.endTime) AND (:end > b.startTime))")
    boolean existsOverlap(@Param("machineId") Long machineId,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.startTime >= :start AND b.endTime <= :end")
    List<Booking> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
}