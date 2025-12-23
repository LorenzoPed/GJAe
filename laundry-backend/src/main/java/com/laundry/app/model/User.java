package com.laundry.app.model;

import jakarta.persistence.*; // Importa tutto il necessario per il database
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity // <--- 1. Questa è la "magia" che crea la tabella
@Table(name = "users") // <--- 2. Meglio chiamare la tabella "users" perché "user" è una parola riservata di SQL
public class User {

    @Id // <--- 3. Dice che questo è l'ID univoco
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <--- 4. Dice a MySQL di creare l'ID da solo (1, 2, 3...)
    private Long id;

    private String name;

    private String email;

    // 'unique = true' to avoid users with same name
    @Column(unique = true)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}