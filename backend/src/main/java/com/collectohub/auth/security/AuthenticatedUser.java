package com.collectohub.auth.security;

import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public final class AuthenticatedUser implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String displayName;
    private final String preferredInterfaceLanguage;
    private final String status;
    private final List<String> roles;
    private final List<GrantedAuthority> authorities;

    private AuthenticatedUser(
            Long id,
            String email,
            String passwordHash,
            String displayName,
            String preferredInterfaceLanguage,
            String status,
            List<String> roles,
            List<GrantedAuthority> authorities
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.preferredInterfaceLanguage = preferredInterfaceLanguage;
        this.status = status;
        this.roles = List.copyOf(roles);
        this.authorities = List.copyOf(authorities);
    }

    public static AuthenticatedUser from(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .sorted()
                .toList();
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .map(GrantedAuthority.class::cast)
                .toList();
        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getDisplayName(),
                user.getPreferredInterfaceLanguage(),
                user.getStatus(),
                roles,
                authorities
        );
    }

    public Long id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String displayName() {
        return displayName;
    }

    public String preferredInterfaceLanguage() {
        return preferredInterfaceLanguage;
    }

    public List<String> roles() {
        return roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return User.STATUS_ACTIVE.equals(status);
    }
}
