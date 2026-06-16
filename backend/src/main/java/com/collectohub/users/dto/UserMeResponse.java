package com.collectohub.users.dto;

import com.collectohub.auth.security.AuthenticatedUser;

import java.util.List;

public record UserMeResponse(
        Long id,
        String email,
        String displayName,
        String preferredInterfaceLanguage,
        List<String> roles
) {

    public static UserMeResponse from(AuthenticatedUser user) {
        return new UserMeResponse(
                user.id(),
                user.email(),
                user.displayName(),
                user.preferredInterfaceLanguage(),
                user.roles()
        );
    }
}
