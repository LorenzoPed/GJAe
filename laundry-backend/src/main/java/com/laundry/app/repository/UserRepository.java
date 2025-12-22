package com.laundry.app.repository;

import com.laundry.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // No need to write anything! JpaRepository has methods
    // save(), findAll(), findById(), delete()...
}