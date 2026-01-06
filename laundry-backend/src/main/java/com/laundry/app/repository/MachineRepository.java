package com.laundry.app.repository;

import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MachineRepository extends JpaRepository<Machine, Long> {

    List<Machine> findByType(MachineType type);

    List<Machine> findByTypeAndEnabledTrue(MachineType type);

    long countByType(MachineType type);

    long countByTypeAndEnabledTrue(MachineType type);
}
