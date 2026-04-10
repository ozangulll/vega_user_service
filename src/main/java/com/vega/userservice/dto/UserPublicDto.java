package com.vega.userservice.dto;

import com.vega.userservice.model.User;
import lombok.Builder;
import lombok.Data;

/**
 * Public user info (no email) for search and directory.
 */
@Data
@Builder
public class UserPublicDto {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;

    public static UserPublicDto fromUser(User user) {
        if (user == null) return null;
        return UserPublicDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
