package com.laundry.app.model;

import jakarta.persistence.*; // Importa tutto il necessario per il database
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * User entity representing application users and their credentials/roles.
 */
@Entity
@Table(name = "users") // Specifies the table name as "users" to avoid conflicts with the SQL reserved keyword 'USER'
public class User {

    @Id // Denotes the primary key of this entity
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures the ID to be auto-incremented by the database (1, 2, 3...)
    private Long id;

    private String name;

    private String email;

    @Column(unique = true, nullable = false) // Enforces uniqueness in the database to prevent duplicate usernames
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * Default constructor for JPA.
     */
    public User() {}

    /**
     * Create a user with basic profile data (password/username/role set later).
     * @param name display name
     * @param email contact email
     */
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters & setters

    /**
     * Returns the user id.
     * @return id
     */
    public Long getId() { return id; }
    /**
     * Set the user id.
     * @param id id to set
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Returns the user's name.
     * @return name
     */
    public String getName() { return name; }
    /**
     * Set the user's name.
     * @param name new name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the user's email.
     * @return email
     */
    public String getEmail() { return email; }
    /**
     * Set the user's email.
     * @param email new email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the username used for login.
     * @return username
     */
    public String getUsername() {
        return username;
    }
    /**
     * Set the username used for login.
     * @param username username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the hashed password.
     * @return password hash
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the hashed password.
     * @param password hashed password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the user's role.
     * @return role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Set the user's role.
     * @param role role to assign
     */
    public void setRole(Role role) {
        this.role = role;
    }
}