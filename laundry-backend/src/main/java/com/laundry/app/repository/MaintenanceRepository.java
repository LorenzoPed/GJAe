package com.laundry.app.repository;

import com.laundry.app.model.Maintenance;
import com.laundry.app.model.MaintenanceStatus;
import com.laundry.app.model.MachineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

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
