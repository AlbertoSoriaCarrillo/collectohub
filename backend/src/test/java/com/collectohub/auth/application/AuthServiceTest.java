package com.collectohub.auth.application;

import com.collectohub.auth.dto.LoginRequest;
import com.collectohub.auth.dto.RegisterRequest;
import com.collectohub.auth.security.JwtService;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import com.collectohub.users.infrastructure.RoleRepository;
import com.collectohub.users.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                roleRepository,
                passwordEncoder,
                jwtService,
                refreshTokenService
        );
    }

    @Test
    void registerCreatesUserWithDefaultRoleAndTokens() {
        Role userRole = new Role("USER", "User");
        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("alice@example.com")).thenReturn(false);
        when(roleRepository.findByCode("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refresh-token");

        var response = authService.register(new RegisterRequest(
                "Alice@Example.com",
                "secret123",
                " Alice ",
                null
        ));

        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.displayName()).isEqualTo("Alice");
        assertThat(response.preferredInterfaceLanguage()).isEqualTo("es");
        assertThat(response.roles()).containsExactly("USER");
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", savedUser.getPasswordHash())).isTrue();
        assertThat(savedUser.getPreferredInterfaceLanguage()).isEqualTo("es");
        assertThat(savedUser.getRoles()).extracting(Role::getCode).containsExactly("USER");
    }

    @Test
    void registerStoresEnglishInterfaceLanguageWhenProvided() {
        Role userRole = new Role("USER", "User");
        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("alice@example.com")).thenReturn(false);
        when(roleRepository.findByCode("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refresh-token");

        var response = authService.register(new RegisterRequest(
                "alice@example.com",
                "secret123",
                "Alice",
                "en"
        ));

        assertThat(response.preferredInterfaceLanguage()).isEqualTo("en");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPreferredInterfaceLanguage()).isEqualTo("en");
    }

    @Test
    void registerNormalizesInterfaceLanguageToLowercase() {
        Role userRole = new Role("USER", "User");
        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("alice@example.com")).thenReturn(false);
        when(roleRepository.findByCode("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refresh-token");

        var response = authService.register(new RegisterRequest(
                "alice@example.com",
                "secret123",
                "Alice",
                "ES"
        ));

        assertThat(response.preferredInterfaceLanguage()).isEqualTo("es");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPreferredInterfaceLanguage()).isEqualTo("es");
    }

    @Test
    void registerRejectsDuplicatedEmail() {
        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest(
                "alice@example.com",
                "secret123",
                "Alice",
                null
        ))).isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsTokensForValidCredentials() {
        User user = User.register(
                "alice@example.com",
                passwordEncoder.encode("secret123"),
                "Alice",
                new Role("USER", "User")
        );
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("alice@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refresh-token");

        var response = authService.login(new LoginRequest("Alice@Example.com", "secret123"));

        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.roles()).containsExactly("USER");
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void loginRejectsInvalidCredentials() {
        User user = User.register(
                "alice@example.com",
                passwordEncoder.encode("secret123"),
                "Alice",
                new Role("USER", "User")
        );
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("alice@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice@example.com", "wrong-password")))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }
}
