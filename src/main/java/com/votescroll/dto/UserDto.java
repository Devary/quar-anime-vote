package com.votescroll.dto;

import com.votescroll.entity.AppUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record UserDto(
        String id,
        String username,
        String email,
        String profilePicture,
        LocalDateTime createdAt,
        List<RoleDto> roles
) {
    public static UserDto from(AppUser u) {
        List<RoleDto> roleDtos = u.roles.stream()
                .map(r -> new RoleDto(r.id, r.name, r.description))
                .sorted((a, b) -> a.id().compareTo(b.id()))
                .collect(Collectors.toList());
        return new UserDto(u.id, u.username, u.email, u.profilePicture, u.createdAt, roleDtos);
    }
}
