package com.collectohub.auth.dto;

import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;

import java.util.List;

public record AuthResponse(
        Long id,
        String email,
        String displayName,
        String preferredInterfaceLanguage,
        List<String> roles,
        String accessToken,
        String refreshToken
) {

    public static AuthResponse from(User user, String accessToken, String refreshToken) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getPreferredInterfaceLanguage(),
                user.getRoles().stream()
                        .map(Role::getCode)
                        .sorted()
                        .toList(),
                accessToken,
                refreshToken
        );
    }
}
