package com.laundry.app.dto;

import com.laundry.app.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component to convert between User DTOs and entity.
 */
@Component
public class UserMapper {

    /**
     * Convert a create request DTO to a User entity.
     * @param request the incoming create request
     * @return a populated User entity
     */
    public User toEntity(UserCreateRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        return user;
    }

    /**
     * Convert a User entity to a response DTO.
     * @param user the user entity
     * @return a UserResponse DTO
     */
    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
