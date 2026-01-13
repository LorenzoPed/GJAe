package com.laundry.app.repository;

import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Machine entities with simple type-based lookups and counters.
 */
public interface MachineRepository extends JpaRepository<Machine, Long> {

    /**
     * Find machines by type.
     * @param type machine type
     * @return list of machines
     */
    List<Machine> findByType(MachineType type);

    /**
     * Find enabled machines by type.
     * @param type machine type
     * @return list of enabled machines
     */
    List<Machine> findByTypeAndEnabledTrue(MachineType type);

    /**
     * Count machines by type.
     * @param type machine type
     * @return count
     */
    long countByType(MachineType type);

    /**
     * Count enabled machines by type.
     * @param type machine type
     * @return count of enabled machines
     */
    long countByTypeAndEnabledTrue(MachineType type);
}
