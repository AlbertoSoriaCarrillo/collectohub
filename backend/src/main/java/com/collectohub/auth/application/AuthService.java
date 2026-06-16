package com.collectohub.auth.application;

import com.collectohub.auth.dto.AuthResponse;
import com.collectohub.auth.dto.LoginRequest;
import com.collectohub.auth.dto.RegisterRequest;
import com.collectohub.auth.security.JwtService;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import com.collectohub.users.infrastructure.RoleRepository;
import com.collectohub.users.infrastructure.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        Role userRole = roleRepository.findByCode(DEFAULT_ROLE)
                .orElseThrow(() -> new RoleNotConfiguredException(DEFAULT_ROLE));
        User user = User.register(
                email,
                passwordEncoder.encode(request.password()),
                request.displayName().trim(),
                userRole
        );
        User savedUser = userRepository.save(user);
        return authenticatedResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .filter(User::isActive)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return authenticatedResponse(user);
    }

    private AuthResponse authenticatedResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return AuthResponse.from(user, accessToken, refreshToken);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
