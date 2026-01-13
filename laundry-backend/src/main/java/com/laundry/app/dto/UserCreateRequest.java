package com.laundry.app.dto;

/**
 * Request DTO for creating a new user.
 *
 * @param name  user name
 * @param email user email
 */
public record UserCreateRequest(String name, String email) {
}
