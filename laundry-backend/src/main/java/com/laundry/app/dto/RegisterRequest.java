// java
package com.laundry.app.dto;

/**
 * DTO used when registering a new user (name, username, email and password).
 */
public class RegisterRequest {
    private String name;
    private String username;
    private String email;
    private String password;

    /**
     * Get the display name for registration.
     *
     * @return the name provided by the user
     */
    public String getName() { return name; }

    /**
     * Set the display name for registration.
     *
     * @param name name to set
     */
    public void setName(String name) { this.name = name; }

    /**
     * Get the username for registration.
     *
     * @return the username
     */
    public String getUsername() { return username; }

    /**
     * Set the username for registration.
     *
     * @param username username to set
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Get the email for registration.
     *
     * @return the email address
     */
    public String getEmail() { return email; }

    /**
     * Set the email for registration.
     *
     * @param email email address to set
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Get the password for registration.
     *
     * @return the raw password
     */
    public String getPassword() { return password; }

    /**
     * Set the password for registration.
     *
     * @param password password to set
     */
    public void setPassword(String password) { this.password = password; }
}
