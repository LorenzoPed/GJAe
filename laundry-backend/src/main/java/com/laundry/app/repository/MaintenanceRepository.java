package com.laundry.app.repository;

import com.laundry.app.model.Maintenance;
import com.laundry.app.model.MaintenanceStatus;
import com.laundry.app.model.MachineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Maintenance records with helpers for overlap and active checks.
 */
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    /**
     * Delete maintenance records by machine id.
     * @param machineId machine id
     */
    void deleteByMachineId(Long machineId);

    /**
     * Check if an overlapping maintenance exists for a machine (excluding cancelled).
     *
     * @param machineId id of the machine to check
     * @param start interval start
     * @param end interval end
     * @param cancelledStatus status representing cancelled records
     * @return true if an overlapping active maintenance exists
     */
    @Query("""
        SELECT COUNT(m) > 0
        FROM Maintenance m
        WHERE m.machine.id = :machineId
          AND m.status <> :cancelledStatus
          AND ((:start < m.endTime) AND (:end > m.startTime))
        """)
    boolean existsOverlap(
        @Param("machineId") Long machineId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("cancelledStatus") MaintenanceStatus cancelledStatus
    );

    /**
     * Check if there's an active maintenance at the given time for a machine.
     *
     * @param machineId machine id
     * @param time time to check
     * @param cancelledStatus status representing cancelled records
     * @return true if an active maintenance exists at the given time
     */
    @Query("""
        SELECT COUNT(m) > 0
        FROM Maintenance m
        WHERE m.machine.id = :machineId
          AND m.status <> :cancelledStatus
          AND m.startTime <= :time
          AND m.endTime > :time
        """)
    boolean existsActiveAt(
        @Param("machineId") Long machineId,
        @Param("time") LocalDateTime time,
        @Param("cancelledStatus") MaintenanceStatus cancelledStatus
    );

    /**
     * Find upcoming maintenances (ending after now) for a machine ordered by start.
     *
     * @param machineId machine id to search for
     * @param now current time, used to filter future maintenances
     * @param cancelledStatus status representing cancelled records
     * @return list of upcoming Maintenance records
     */
    @Query("""
        SELECT m
        FROM Maintenance m
        WHERE m.machine.id = :machineId
          AND m.status <> :cancelledStatus
          AND m.endTime > :now
        ORDER BY m.startTime
        """)
    List<Maintenance> findUpcoming(
        @Param("machineId") Long machineId,
        @Param("now") LocalDateTime now,
        @Param("cancelledStatus") MaintenanceStatus cancelledStatus
    );

    /**
     * Return machine ids that are under maintenance at the given time (excluding cancelled).
     *
     * @param time time to check
     * @param cancelledStatus status representing cancelled records
     * @return list of machine ids under maintenance at the given time
     */
    @Query("""
        SELECT DISTINCT m.machine.id
        FROM Maintenance m
        WHERE m.status <> :cancelledStatus
          AND m.startTime <= :time
          AND m.endTime > :time
        """)
    List<Long> findMachineIdsUnderMaintenanceAt(
        @Param("time") LocalDateTime time,
        @Param("cancelledStatus") MaintenanceStatus cancelledStatus
    );

    /**
     * Count distinct enabled machines of a given type that have overlapping maintenance in the interval.
     *
     * @param type machine type to filter
     * @param start interval start
     * @param end interval end
     * @param cancelledStatus status representing cancelled records
     * @return number of distinct machines of the given type with overlapping maintenance
     */
    @Query("""
        SELECT COUNT(DISTINCT m.machine.id)
        FROM Maintenance m
        WHERE m.machine.type = :type
          AND m.machine.enabled = true
          AND m.status <> :cancelledStatus
          AND ((:start < m.endTime) AND (:end > m.startTime))
        """)
    long countMachinesWithOverlappingMaintenanceByType(
        @Param("type") MachineType type,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("cancelledStatus") MaintenanceStatus cancelledStatus
    );
}
