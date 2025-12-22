package com.laundry.app.model;

import jakarta.persistence.*; // Importa tutto il necessario per il database

@Entity // <--- 1. Questa è la "magia" che crea la tabella
@Table(name = "users") // <--- 2. Meglio chiamare la tabella "users" perché "user" è una parola riservata di SQL
public class User {

    @Id // <--- 3. Dice che questo è l'ID univoco
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <--- 4. Dice a MySQL di creare l'ID da solo (1, 2, 3...)
    private Long id; // Ho cambiato da String a Long perché MySQL lavora meglio con i numeri automatici

    private String name;
    private String email;

    // Costruttore vuoto (obbligatorio per JPA)
    public User() {}

    // Costruttore pieno
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


}