package com.laundry.app.repository;

import com.laundry.app.model.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
    // Standard CRUD operations are already included (save, findById, etc.)
}