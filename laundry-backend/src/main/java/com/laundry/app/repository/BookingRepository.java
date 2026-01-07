package com.laundry.app.repository;

import com.laundry.app.model.Booking;
import com.laundry.app.model.BookingStatus;
import com.laundry.app.model.MachineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser_Username(String username);

    List<Booking> findByUser_UsernameAndStatusNot(String username, BookingStatus status);

    List<Booking> findByStatusNot(BookingStatus status);

    List<Booking> findByMachineId(Long machineId);

    List<Booking> findByUserId(Long userId);

    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.machine.id = :machineId
          AND b.status <> :excludedStatus
          AND ((:start < b.endTime) AND (:end > b.startTime))
        """)
    boolean existsOverlap(
        @Param("machineId") Long machineId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("excludedStatus") BookingStatus excludedStatus
    );

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.startTime >= :start AND b.endTime <= :end
        """)
    List<Booking> findByDateRange(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.machine.id = :machineId
          AND b.status <> :excludedStatus
          AND ((:start < b.endTime) AND (:end > b.startTime))
        ORDER BY b.startTime
        """)
    List<Booking> findOverlappingBookingsForMachine(
        @Param("machineId") Long machineId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("excludedStatus") BookingStatus excludedStatus
    );

    @Query("""
        SELECT COUNT(b)
        FROM Booking b
        WHERE b.machine.type = :type
          AND b.machine.enabled = true
          AND b.status <> :excludedStatus
          AND ((:start < b.endTime) AND (:end > b.startTime))
        """)
    long countConflictingBookings(
        @Param("type") MachineType type,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("excludedStatus") BookingStatus excludedStatus
    );

    /**
     * Used when a machine is disabled:
     * returns all future (or ongoing) bookings that still depend on that machine.
     */
    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.machine.id = :machineId
          AND b.status <> :excludedStatus
          AND b.endTime > :from
        ORDER BY b.startTime
        """)
    List<Booking> findFutureBookingsForMachine(
        @Param("machineId") Long machineId,
        @Param("from") LocalDateTime from,
        @Param("excludedStatus") BookingStatus excludedStatus
    );
}
