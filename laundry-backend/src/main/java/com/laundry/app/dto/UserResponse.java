package com.laundry.app.dto;

/**
 * Response DTO for user data.
 *
 * @param id user identifier
 * @param name user name
 * @param email user email
 */
public record UserResponse(Long id, String name, String email) {
}
