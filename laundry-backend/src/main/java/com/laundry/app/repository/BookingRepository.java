package com.laundry.app.repository;

import com.laundry.app.model.Booking;
import com.laundry.app.model.BookingStatus;
import com.laundry.app.model.MachineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.laundry.app.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Booking entities with convenience queries for overlaps, ranges and user/machine lookups.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find bookings belonging to a user by username.
     * @param username the user's username
     * @return list of bookings
     */
    List<Booking> findByUser_Username(String username);

    /**
     * Find bookings for a user excluding a specific status.
     * @param username the user's username
     * @param status the status to exclude
     * @return list of bookings
     */
    List<Booking> findByUser_UsernameAndStatusNot(String username, BookingStatus status);

    /**
     * Find bookings that are not in the given status.
     * @param status status to exclude
     * @return list of bookings
     */
    List<Booking> findByStatusNot(BookingStatus status);

    /**
     * Retrieve bookings for a user ordered by start time descending.
     * @param user the user entity
     * @return ordered list of bookings
     */
    List<Booking> findByUserOrderByStartTimeDesc(User user);

    /**
     * Find bookings for a given machine id.
     * @param machineId machine id
     * @return list of bookings
     */
    List<Booking> findByMachineId(Long machineId);

    /**
     * Find bookings for a given user id.
     * @param userId user id
     * @return list of bookings
     */
    List<Booking> findByUserId(Long userId);

    /**
     * Delete all bookings for the specified machine.
     * @param machineId machine id
     */
    void deleteByMachineId(Long machineId);

    /**
     * Check whether there exists a non-excluded booking that overlaps the given interval for a machine.
     */
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

    /**
     * Find bookings that are fully contained in the given date range.
     */
    @Query("SELECT b FROM Booking b WHERE b.startTime >= :start AND b.endTime <= :end")
    List<Booking> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Return bookings overlapping the interval for a machine ordered by start time.
     */
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

    /**
     * Count conflicting bookings for machines of a given type in the interval.
     */
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
     * Used when a machine gets disabled: find only future bookings (after now) to be impacted.
     */
    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.machine.id = :machineId
          AND b.status <> :excludedStatus
          AND b.startTime > :now
        ORDER BY b.startTime
        """)
    List<Booking> findFutureBookingsForMachine(
        @Param("machineId") Long machineId,
        @Param("now") LocalDateTime now,
        @Param("excludedStatus") BookingStatus excludedStatus
    );


}
