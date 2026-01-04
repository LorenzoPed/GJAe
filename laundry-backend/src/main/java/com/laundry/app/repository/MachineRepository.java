package com.laundry.app.repository;

import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType; // <--- Importante
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MachineRepository extends JpaRepository<Machine, Long> {

    List<Machine> findByType(MachineType type);
    long countByType(MachineType type);
}