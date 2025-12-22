package com.laundry.app.repository;
import java.util.Optional;

import com.laundry.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 1. TROVA UTENTE PER LOGIN (Fondamentale)
    // Spring Security userà questo per caricare i dati dell'utente che prova a entrare
    Optional<User> findByUsername(String username);

    // 2. CHECK ESISTENZA (Utili per la Registrazione)
    // Per evitare che due persone usino lo stesso nome o mail
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}